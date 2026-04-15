package com.example.ds_safer.ui.screens.discovery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.JetsonRegisterRequest
import com.example.ds_safer.data.api.RetrofitClient
import AuthDataStore
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.util.nsd.NsdHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DiscoveryViewModel(
    private val nsdHelper: NsdHelper,
    private val authDataStore: AuthDataStore
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

    init {
        startScan()
    }

    fun startScan() {
        _isScanning.value = true
        nsdHelper.startDiscovery(object : NsdHelper.NsdDiscoveryListener {
            override fun onDeviceFound(device: JetsonDevice) {
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

                // 2. Retrofit 서비스 생성
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                // 3. 서버에 POST 통신으로 등록 요청
                val request = JetsonRegisterRequest(dept_id = deptId, app_id = "app1")
                val response = service.registerJetson(request)

                if (response.isSuccessful && response.body()?.register_status == "success") {
                    val responseBody = response.body()!!
                    Log.d("API_TEST", "젯슨 등록 성공! (웹소켓 연결은 Manager가 자동으로 수행합니다)")

                    val rawIdString = responseBody.jetson_id
                    val parsedId = rawIdString.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0

                    // 4. 기기 상태 업데이트
                    val newDevice = device.copy(
                        jetsonId = parsedId,
                        status = true
                    )

                    // 5. UI 리스트 업데이트 (하단 -> 상단 이동)
                    // 이 순간 MainDashboardScreen이 변화를 감지하고 WebSocketManager를 통해 웹소켓을 연결함!
                    _discoveredJetsons.value = _discoveredJetsons.value.filter { it.ipAddress != device.ipAddress }
                    _registeredJetsons.value = _registeredJetsons.value + newDevice

                    // 6. 전역 저장소에 등록
                    JetsonRepository.selectJetson(newDevice)

                } else {
                    Log.e("API_ERROR", "서버 응답 에러: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                Log.e("API_ERROR", "통신 실패: 젯슨 서버 연결 확인 요망", e)
            }
        }
    }
}