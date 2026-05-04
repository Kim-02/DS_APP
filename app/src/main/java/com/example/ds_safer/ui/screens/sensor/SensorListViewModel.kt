
package com.example.ds_safer.ui.screens.sensor

import android.util.Log
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

    init {
        Log.d("SensorListVM", "ViewModel created")
    }

    fun fetchSensors() {
        Log.d("SensorListVM", "fetchSensors called")

        val device = JetsonRepository.selectedJetson.value
        if (device == null) {
            Log.d("SensorListVM", "selectedJetson is null")
            _sensorList.value = emptyList()
            _errorMessage.value = "선택된 젯슨 정보가 없습니다."
            return
        }

        Log.d("SensorListVM", "selectedJetson = $device")

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                Log.d("SensorListVM", "baseUrl = $baseUrl")

                val service = RetrofitClient.createService(baseUrl)
                Log.d("SensorListVM", "service created")

                val response = service.getRegisteredSensors()
                Log.d("SensorListVM", "REGISTERED response.status = ${response.status}")
                Log.d("SensorListVM", "REGISTERED response.data = ${response.data}")
                Log.d("SensorListVM", "REGISTERED response size = ${response.data.size}")

                if (response.status == "success") {
                    _sensorList.value = response.data
                    Log.d("SensorListVM", "_sensorList updated = ${_sensorList.value}")
                } else {
                    _sensorList.value = emptyList()
                    _errorMessage.value = "등록된 센서 목록을 불러오지 못했습니다."
                    Log.d("SensorListVM", "status not success")
                }
            } catch (e: Exception) {
                Log.e("SensorListVM", "fetchSensors error", e)
                _sensorList.value = emptyList()
                _errorMessage.value = "등록된 센서 목록 조회 중 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
                Log.d("SensorListVM", "fetchSensors finished")
            }
        }
    }

    fun unregisterSensor(sensorId: String) {
        Log.d("SensorListVM", "unregisterSensor called, sensorId = $sensorId")

        val device = JetsonRepository.selectedJetson.value
        if (device == null) {
            Log.d("SensorListVM", "selectedJetson is null in unregister")
            _errorMessage.value = "선택된 젯슨 정보가 없습니다."
            return
        }

        viewModelScope.launch {
            _isUnregistering.value = true
            _errorMessage.value = null

            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                Log.d("SensorListVM", "unregister baseUrl = $baseUrl")

                val service = RetrofitClient.createService(baseUrl)
                val response = service.unregisterSensor(
                    SensorUnregisterRequest(sensorId = sensorId)
                )

                Log.d("SensorListVM", "UNREGISTER response.status = ${response.status}")
                Log.d("SensorListVM", "UNREGISTER response.message = ${response.message}")

                if (response.status == "success") {
                    fetchSensors()
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                Log.e("SensorListVM", "unregisterSensor error", e)
                _errorMessage.value = "센서 등록 해제 중 오류가 발생했습니다."
            } finally {
                _isUnregistering.value = false
                Log.d("SensorListVM", "unregisterSensor finished")
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}