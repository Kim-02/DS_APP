package com.example.ds_safer.ui.screens.sensor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.RegisteredSensor
import com.example.ds_safer.domain.model.SensorUnregisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SensorListViewModel : ViewModel() {
    private val _sensorList = MutableStateFlow<List<RegisteredSensor>>(emptyList())
    val sensorList = _sensorList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isUnregistering = MutableStateFlow(false)
    val isUnregistering = _isUnregistering.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun fetchSensors() {
        val device = JetsonRepository.selectedJetson.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val response = service.getRegisteredSensors()
                if (response.status == "success") {
                    _sensorList.value = response.data
                } else {
                    _sensorList.value = emptyList()
                    _errorMessage.value = "등록된 센서 목록을 불러오지 못했습니다."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _sensorList.value = emptyList()
                _errorMessage.value = "등록된 센서 목록 조회 중 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun unregisterSensor(sensorId: String) {
        val device = JetsonRepository.selectedJetson.value ?: return

        viewModelScope.launch {
            _isUnregistering.value = true
            _errorMessage.value = null

            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val response = service.unregisterSensor(
                    SensorUnregisterRequest(sensor_id = sensorId)
                )

                if (response.status == "success") {
                    fetchSensors()
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "센서 등록 해제 중 오류가 발생했습니다."
            } finally {
                _isUnregistering.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
