package com.example.ds_safer.ui.screens.sensor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.api.SensorRegisterRequest
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.SensorInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SensorRegistrationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SensorUiState>(SensorUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchSensors()
    }

    fun fetchSensors() {
        val device = JetsonRepository.selectedJetson.value
        if (device == null) {
            _uiState.value = SensorUiState.Error("등록된 젯슨이 없습니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = SensorUiState.Loading
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)
                val sensors = service.getReadySensors()
                _uiState.value = SensorUiState.Success(sensors)
            } catch (e: Exception) {
                _uiState.value = SensorUiState.Error("목록을 가져오지 못했습니다: ${e.message}")
            }
        }
    }

    fun registerSensor(sensor: SensorInfo) {
        val device = JetsonRepository.selectedJetson.value ?: return

        viewModelScope.launch {
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                // 젯슨에게 POST 요청 전송
                val response = service.registerSensor(SensorRegisterRequest(sensor.id))

                if (response.isSuccessful) {
                    // 성공하면 목록 새로고침 (혹은 리스트에서 제거)
                    fetchSensors()
                    Log.d("API", "센서 등록 성공: ${sensor.id}")
                }
            } catch (e: Exception) {
                Log.e("API", "센서 등록 실패", e)
            }
        }
    }
}

sealed class SensorUiState {
    object Loading : SensorUiState()
    data class Success(val sensors: List<SensorInfo>) : SensorUiState()
    data class Error(val message: String) : SensorUiState()
}