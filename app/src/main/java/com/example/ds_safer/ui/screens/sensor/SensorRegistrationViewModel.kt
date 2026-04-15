package com.example.ds_safer.ui.screens.sensor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.DiscoveredSensor
import com.example.ds_safer.domain.model.SensorRegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SensorRegistrationViewModel : ViewModel() {

    private val _readySensors = MutableStateFlow<List<DiscoveredSensor>>(emptyList())
    val readySensors = _readySensors.asStateFlow()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess = _registerSuccess.asStateFlow()

    init {
        fetchReadySensors()
    }

    fun fetchReadySensors() {
        val device = JetsonRepository.selectedJetson.value ?: return
        viewModelScope.launch {
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val response = service.getDiscoverSensors()

                // 서버 응답 구조: { status, data }
                _readySensors.value = response.data

            } catch (e: Exception) {
                _readySensors.value = emptyList()
                e.printStackTrace()
            }
        }
    }

    fun registerMultipleSensors(selectedList: List<DiscoveredSensor>) {
        if (selectedList.isEmpty()) return

        val device = JetsonRepository.selectedJetson.value ?: return
        val actualJetsonId = device.jetsonId ?: 1

        viewModelScope.launch {
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val requestBody = SensorRegisterRequest(
                    jetsonId = "jetson-$actualJetsonId",
                    selectedSensors = selectedList
                )

                val response = service.registerSensor(requestBody)

                if (response.status == "success") {
                    println("✅ 센서 다중 등록 성공: ${response.message}")
                    _registerSuccess.value = true
                    fetchReadySensors()
                } else {
                    println("❌ 서버 응답 실패: ${response.message}")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun consumeRegisterSuccess() {
        _registerSuccess.value = false
    }
}
