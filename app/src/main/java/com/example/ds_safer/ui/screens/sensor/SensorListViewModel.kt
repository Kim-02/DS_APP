package com.example.ds_safer.ui.screens.sensor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.RegisteredSensor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SensorListViewModel : ViewModel() {
    private val _sensorList = MutableStateFlow<List<RegisteredSensor>>(emptyList())
    val sensorList = _sensorList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchSensors() {
        val device = JetsonRepository.selectedJetson.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                // API 호출
                val response = service.getRegisteredSensors()
                if (response.status == "success") {
                    _sensorList.value = response.data
                } else {
                    _sensorList.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _sensorList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}