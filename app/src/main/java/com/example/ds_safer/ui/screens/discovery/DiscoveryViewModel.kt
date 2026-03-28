package com.example.ds_safer.ui.screens.discovery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.util.nsd.NsdHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections

class DiscoveryViewModel(application: Application) : AndroidViewModel(application) {

    // UI에서 관찰할 젯슨 기기 리스트 상태
    private val _discoveredDevices = MutableStateFlow<List<JetsonDevice>>(emptyList())
    val discoveredDevices = _discoveredDevices.asStateFlow()

    init {
        // [테스트용] 앱 실행하자마자 가짜 데이터 하나 넣기
        _discoveredDevices.value = listOf(
            JetsonDevice(
                name = "Jetson-Test-Device",
                ipAddress = "192.168.0.10",
                port = 8080,
                isConnected = true // 초록색 불 들어오게 설정
            )
        )
    }

    // 로딩 상태 (검색 중 표시용)
    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val nsdHelper = NsdHelper(application.applicationContext)

    // 중복 추가 방지를 위한 Set (thread-safe)
    private val knownDeviceHosts = Collections.synchronizedSet(mutableSetOf<String>())

    private val nsdListener = object : NsdHelper.NsdDiscoveryListener {
        override fun onDeviceFound(device: JetsonDevice) {
            // 이미 찾은 IP의 기기라면 스킵
            if (knownDeviceHosts.contains(device.ipAddress)) return

            knownDeviceHosts.add(device.ipAddress)

            // 1. 리스트에 먼저 추가 (기본 상태)
            _discoveredDevices.update { it + device }

            // 2. 설계도대로 Health Check API 호출 시도
            checkDeviceHealth(device)
        }

        override fun onDiscoveryStarted() {
            _isScanning.value = true
        }

        override fun onDiscoveryStopped() {
            _isScanning.value = false
        }
    }

    fun startScanning() {
        // 기존 리스트 초기화
        _discoveredDevices.value = emptyList()
        knownDeviceHosts.clear()
        nsdHelper.startDiscovery(nsdListener)
    }

    fun stopScanning() {
        nsdHelper.stopDiscovery()
    }

    // 설계도의 "API 요청 전달 (GET /api/health)" 부분 구현
    private fun checkDeviceHealth(device: JetsonDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 동적으로 URL 생성
                val baseUrl = "http://${device.ipAddress}:${device.port}/"

                // jetson의 IP로 통신 통로 만듦
                val service = RetrofitClient.createService(baseUrl)

                // 설계도의 "API 응답" 대기
                // jetson이 보낸 결과값이 담김
                val response = service.checkHealth()

                // 응답 성공 시 (예외 안 나면 성공으로 간주)
                if (response.status.isNotEmpty()) {
                    // 설계도의 "jetson 정보 저장" -> UI 상태 업데이트
                    updateDeviceStatus(device.ipAddress, true)
                }
            } catch (e: Exception) {
                // 네트워크 오류 (타임아웃 등) 시 연결 실패로 처리
                e.printStackTrace()
                updateDeviceStatus(device.ipAddress, false)
            }
        }
    }

    // 특정 기기의 연결 상태만 업데이트하는 함수
    private fun updateDeviceStatus(hostAddress: String, isConnected: Boolean) {
        _discoveredDevices.update { currentList ->
            currentList.map {
                if (it.ipAddress == hostAddress) it.copy(isConnected = isConnected)
                else it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning() // 뷰모델 소멸 시 mDNS 탐색 종료 (필수)
    }
}