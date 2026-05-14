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

    private val _registeredJetsons = MutableStateFlow<List<JetsonDevice>>(emptyList())
    val registeredJetsons = _registeredJetsons.asStateFlow()

    private val _discoveredJetsons = MutableStateFlow<List<JetsonDevice>>(emptyList())
    val discoveredJetsons = _discoveredJetsons.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    init {
        startScan()
    }

    fun startScan() {
        _isScanning.value = true

        nsdHelper.startDiscovery(object : NsdHelper.NsdDiscoveryListener {
            override fun onDeviceFound(device: JetsonDevice) {
                val current = _discoveredJetsons.value
                if (current.none { it.ipAddress == device.ipAddress && it.port == device.port }) {
                    _discoveredJetsons.value = current + device
                }

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

    fun loadRegisteredJetsonsFromDiscoveredServers() {
        viewModelScope.launch {
            val discoveredServers = _discoveredJetsons.value

            if (discoveredServers.isEmpty()) {
                return@launch
            }

            val result = mutableListOf<JetsonDevice>()

            for (server in discoveredServers) {
                try {
                    val service = RetrofitClient.createService(
                        "http://${server.ipAddress}:${server.port}/"
                    )

                    val rows = service.getRegisteredJetsonsV1()

                    rows.forEach { row ->
                        if (row.jetsonStatus) {
                            var sensorTotal = 0
                            var cctvTotal = 0
                            var workerTotal = 0

                            val spaceId = row.spaceId
                            if (spaceId != null && spaceId > 0) {
                                try {
                                    val summary = service.getDashboardSummary(spaceId)
                                    summary.data?.let {
                                        sensorTotal = it.sensorTotal
                                        cctvTotal = it.cctvTotal
                                        workerTotal = it.workerTotal
                                    }
                                } catch (_: Exception) { }
                            }

                            result.add(
                                JetsonDevice(
                                    name = row.jetsonWp,
                                    ipAddress = row.ipAddr,
                                    port = row.port,
                                    status = row.jetsonStatus,
                                    jetsonId = row.jetsonId,
                                    spaceId = row.spaceId,
                                    spaceName = row.spaceName,
                                    isRegistered = true,
                                    sensorTotal = sensorTotal,
                                    cctvTotal = cctvTotal,
                                    workerTotal = workerTotal
                                )
                            )
                        }
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
        }
    }

    @Deprecated("Jetson 신규 등록은 JetsonSpaceRegisterScreen에서 space_id와 함께 처리하세요.")
    fun registerDevice(device: JetsonDevice) {
        viewModelScope.launch {
            try {
                val deptIdString = authDataStore.authFlow.first().deptId
                val deptId = deptIdString.toIntOrNull() ?: 0

                val service = RetrofitClient.createService(
                    "http://${device.ipAddress}:${device.port}/"
                )

                val request = JetsonRegisterRequest(dept_id = deptId, app_id = "app1")
                val response = service.registerJetson(request)

                if (response.isSuccessful && response.body()?.register_status == "success") {
                    val body = response.body()!!
                    val parsedId = body.jetson_id
                        .replace(Regex("[^0-9]"), "")
                        .toIntOrNull() ?: 0

                    val newDevice = device.copy(
                        jetsonId = parsedId,
                        status = true,
                        isRegistered = true
                    )

                    JetsonRepository.selectJetson(newDevice)
                    loadRegisteredJetsonsFromDiscoveredServers()
                }
            } catch (e: Exception) {
                Log.e("DiscoveryViewModel", "구형 Jetson 등록 실패", e)
            }
        }
    }
}