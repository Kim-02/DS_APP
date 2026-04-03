package com.example.ds_safer.ui.screens.discovery // 패키지 경로 확인!

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.JetsonRegisterRequest // ★ 추가된 DTO
import com.example.ds_safer.data.api.RetrofitClient
import AuthDataStore // ★ 추가된 로컬 저장소
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.util.nsd.NsdHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class DiscoveryViewModel(
    private val nsdHelper: NsdHelper,
    private val authDataStore: AuthDataStore // ★ 추가: 사번을 꺼내오기 위해 주입
) : ViewModel() {

    // 📱 상단: 이미 등록된 젯슨 목록
    private val _registeredJetsons = MutableStateFlow<List<JetsonDevice>>(emptyList())
    val registeredJetsons = _registeredJetsons.asStateFlow()

    // 📡 하단: 현재 레이더(mDNS)로 찾은 새로운 젯슨 목록
    private val _discoveredJetsons = MutableStateFlow<List<JetsonDevice>>(emptyList())
    val discoveredJetsons = _discoveredJetsons.asStateFlow()

    // 🔄 상태 관리: 로딩 중인지 확인
    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    // ==========================================
    // ★ 웹소켓 통신을 위한 클라이언트 및 세션 변수
    // ==========================================
    private var activeWebSocket: WebSocket? = null
    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS) // 연결이 끊기지 않게 핑을 보냄
        .build()

    init {
        startScan()
    }

    fun startScan() {
        _isScanning.value = true
        nsdHelper.startDiscovery(object : NsdHelper.NsdDiscoveryListener {
            override fun onDeviceFound(device: JetsonDevice) {
                // 이미 등록된 리스트에 없는 기기만 '발견된 기기'에 추가
                if (_registeredJetsons.value.none { it.ipAddress == device.ipAddress }) {
                    val currentList = _discoveredJetsons.value
                    if (currentList.none { it.ipAddress == device.ipAddress }) {
                        _discoveredJetsons.value = currentList + device
                    }
                }
            }
            override fun onDiscoveryStarted() { _isScanning.value = true }
            override fun onDiscoveryStopped() { _isScanning.value = false }
        })
    }

    // ★ [등록] 버튼 클릭 시 호출되는 원클릭 로직
    fun registerDevice(device: JetsonDevice) {
        viewModelScope.launch {
            try {
                // 1. DataStore에서 로그인할 때 저장해둔 사번(dept_id) 꺼내오기
                val deptIdString = authDataStore.authFlow.first().deptId
                val deptId = deptIdString.toIntOrNull() ?: 0

                // 2. Retrofit 서비스 생성 (기기 IP에 맞춰 동적 생성)
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                // RetrofitClient 내부 구현에 따라 반환 타입이 JetsonApiService여야 합니다.
                val service = RetrofitClient.createService(baseUrl)

                // 3. 서버에 POST 통신으로 등록 요청 (GET -> POST로 변경됨)
                val request = JetsonRegisterRequest(dept_id = deptId, app_id = "app1")
                val response = service.registerJetson(request)

                Log.d("API_TEST", "HTTP 상태 코드: ${response.code()}")
                Log.d("API_TEST", "서버가 준 실제 데이터: ${response.body()}")

                if (response.isSuccessful && response.body()?.register_status == "success") {
                    val responseBody = response.body()!!
                    Log.d("API_TEST", "젯슨 등록 성공! 웹소켓 URL: ${responseBody.ws_url}")

                    // 4. 응답받은 웹소켓 주소로 파이프 연결 시작!
                    connectWebSocket(responseBody.ws_url)

                    val rawIdString = responseBody.jetson_id
                    val parsedId = rawIdString.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0

                    // 5. 서버가 내려준 진짜 ID(예: "jetson-01")를 반영하여 기기 상태 업데이트
                    // (주의: JetsonDevice 모델의 jetsonId가 Int형이라면 String으로 자료형을 바꿔주셔야 합니다!)
                    val newDevice = device.copy(
                        jetsonId = parsedId, // ★ DB에서 준 ID
                        status = true
                    )

                    // 6. UI 리스트 업데이트 (하단 -> 상단 이동)
                    _discoveredJetsons.value = _discoveredJetsons.value.filter { it.ipAddress != device.ipAddress }
                    _registeredJetsons.value = _registeredJetsons.value + newDevice

                    // 7. 전역 저장소에 등록
                    JetsonRepository.selectJetson(newDevice)

                } else {
                    Log.e("API_ERROR", "서버 응답 에러: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                Log.e("API_ERROR", "통신 실패: 젯슨 서버 연결 확인 요망", e)
            }
        }
    }

    // ★ 웹소켓 연결 및 이벤트 리스너
    private fun connectWebSocket(wsUrl: String) {
        val request = Request.Builder().url(wsUrl).build()

        val webSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                Log.d("WS_TEST", "✅ 웹소켓 연결 성공!")
                activeWebSocket = webSocket
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WS_TEST", "📩 젯슨 데이터 수신: $text")
                // TODO: 여기서 JSON 파싱해서 "위험" 알림이면 화면에 띄우기
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                Log.e("WS_TEST", "❌ 웹소켓 에러 발생", t)
                activeWebSocket = null
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WS_TEST", "🔌 웹소켓 종료됨")
                activeWebSocket = null
            }
        }

        okHttpClient.newWebSocket(request, webSocketListener)
    }

    // 뷰모델이 파괴될 때 웹소켓도 안전하게 닫아줍니다.
    override fun onCleared() {
        super.onCleared()
        activeWebSocket?.close(1000, "앱 종료 또는 화면 이탈")
    }
}