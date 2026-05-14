package com.example.ds_safer.ui.screens.discovery

import AuthDataStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.JetsonRegisterRequest
import com.example.ds_safer.data.api.RetrofitClient
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

    // DB에 등록된 Jetson만 메인 화면에 표시
    private val _registeredJetsons = MutableStateFlow<List<JetsonDevice>>(emptyList())
    val registeredJetsons = _registeredJetsons.asStateFlow()

    // mDNS로 발견된 Jetson은 등록 페이지에서만 사용
    private val _discoveredJetsons = MutableStateFlow<List<JetsonDevice>>(emptyList())
    val discoveredJetsons = _discoveredJetsons.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _isLoadingRegisteredJetsons = MutableStateFlow(false)
    val isLoadingRegisteredJetsons = _isLoadingRegisteredJetsons.asStateFlow()

    init {
        startScan()
    }

    fun startScan() {
        _isScanning.value = true

        nsdHelper.startDiscovery(object : NsdHelper.NsdDiscoveryListener {
            override fun onDeviceFound(device: JetsonDevice) {
                val currentList = _discoveredJetsons.value

                if (currentList.none { it.ipAddress == device.ipAddress && it.port == device.port }) {
                    _discoveredJetsons.value = currentList + device
                }

                // mDNS 서버가 발견되면, 그 서버의 DB 등록 Jetson 목록을 다시 조회
                loadRegisteredJetsonsFromDiscoveredServers()
            }

            override fun onDiscoveryStarted() {
                _isScanning.value = true
            }

            override fun onDiscoveryStopped() {
                _isScanning.value = false
            }
        })
    }

    /**
     * 메인 화면용.
     *
     * mDNS로 발견된 서버에 접속해서 /api/v1/jetsons를 조회하고,
     * DB에 등록된 Jetson만 registeredJetsons에 넣는다.
     *
     * 따라서 메인 화면에는 더 이상 mDNS 발견 목록이 직접 표시되지 않는다.
     */
    fun loadRegisteredJetsonsFromDiscoveredServers() {
        viewModelScope.launch {
            _isLoadingRegisteredJetsons.value = true

            try {
                val discoveredServers = _discoveredJetsons.value

                if (discoveredServers.isEmpty()) {
                    _registeredJetsons.value = emptyList()
                    return@launch
                }

                val result = mutableListOf<JetsonDevice>()

                for (server in discoveredServers) {
                    try {
                        val api = RetrofitClient.createService(
                            "http://${server.ipAddress}:${server.port}"
                        )

                        val rows = api.getRegisteredJetsonsV1()

                        rows.forEach { row ->
                            // 비활성화된 Jetson까지 보여주고 싶으면 이 if 제거
                            if (!row.jetsonStatus) return@forEach

                            result.add(
                                JetsonDevice(
                                    name = row.jetsonWp,
                                    ipAddress = row.ipAddr,
                                    port = row.port,
                                    status = row.jetsonStatus,
                                    jetsonId = row.jetsonId,
                                    spaceId = row.spaceId,
                                    spaceName = row.spaceName,
                                    isRegistered = true
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "DiscoveryViewModel",
                            "등록 Jetson 조회 실패: ${server.ipAddress}:${server.port}",
                            e
                        )
                    }
                }

                _registeredJetsons.value = result.distinctBy {
                    "${it.ipAddress}:${it.port}"
                }
            } finally {
                _isLoadingRegisteredJetsons.value = false
            }
        }
    }

    /**
     * 구형 등록 함수.
     * 이제 mDNS Jetson 등록에는 사용하지 않는다.
     * Jetson-space 매핑 등록은 JetsonSpaceRegisterViewModel.registerSelectedJetson()을 사용한다.
     *
     * 기존 코드에서 참조하고 있을 수 있어 일단 남겨둔다.
     */
    @Deprecated("Jetson 신규 등록은 /api/v1/jetsons/register를 사용하는 JetsonSpaceRegisterViewModel에서 처리하세요.")
    fun registerDevice(device: JetsonDevice) {
        viewModelScope.launch {
            try {
                val deptIdString = authDataStore.authFlow.first().deptId
                val deptId = deptIdString.toIntOrNull() ?: 0

                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val request = JetsonRegisterRequest(dept_id = deptId, app_id = "app1")
                val response = service.registerJetson(request)

                if (response.isSuccessful && response.body()?.register_status == "success") {
                    val responseBody = response.body()!!
                    val rawIdString = responseBody.jetson_id
                    val parsedId = rawIdString.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0

                    val newDevice = device.copy(
                        jetsonId = parsedId,
                        status = true,
                        isRegistered = true
                    )

                    JetsonRepository.selectJetson(newDevice)
                    loadRegisteredJetsonsFromDiscoveredServers()
                } else {
                    Log.e("API_ERROR", "구형 Jetson 등록 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "구형 Jetson 등록 통신 실패", e)
            }
        }
    }
}