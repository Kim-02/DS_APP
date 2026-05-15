package com.example.ds_safer.ui.screens.discovery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.util.nsd.NsdHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiscoveryViewModel(
    private val nsdHelper: NsdHelper
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

}