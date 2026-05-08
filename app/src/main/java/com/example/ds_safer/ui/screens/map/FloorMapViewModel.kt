package com.example.ds_safer.ui.screens.floormap

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.FloorMapInfo
import com.example.ds_safer.domain.model.RegisteredSensor
import com.example.ds_safer.domain.model.SaveSensorPositionRequest
import com.example.ds_safer.domain.model.SensorMapPosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FloorMapViewModel : ViewModel() {

    private val _floorMap = MutableStateFlow<FloorMapInfo?>(null)
    val floorMap = _floorMap.asStateFlow()

    private val _availableSensors = MutableStateFlow<List<RegisteredSensor>>(emptyList())
    val availableSensors = _availableSensors.asStateFlow()

    private val _placedSensors = MutableStateFlow<List<SensorMapPosition>>(emptyList())
    val placedSensors = _placedSensors.asStateFlow()

    private val _selectedSensor = MutableStateFlow<RegisteredSensor?>(null)
    val selectedSensor = _selectedSensor.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun selectSensor(sensor: RegisteredSensor) {
        _selectedSensor.value = sensor
    }

    fun clearMessage() {
        _message.value = null
    }

    fun loadAll() {
        val device = JetsonRepository.selectedJetson.value ?: return
        val jetsonId = device.jetsonId ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val mapResponse = service.getFloorMap(jetsonId)
                if (mapResponse.status == "success") {
                    _floorMap.value = mapResponse.data
                } else {
                    _message.value = "평면도 정보를 불러오지 못했습니다."
                }

                // 온습도 센서만 배치 대상으로 조회
                val sensorResponse = service.getAvailableTempSensorsForMap(jetsonId)
                if (sensorResponse.status == "success") {
                    _availableSensors.value = sensorResponse.data
                } else {
                    _availableSensors.value = emptyList()
                }

                _floorMap.value?.let { mapInfo ->
                    val placedResponse = service.getMapSensorPositions(mapInfo.mapId)
                    if (placedResponse.status == "success") {
                        _placedSensors.value = placedResponse.data
                    } else {
                        _placedSensors.value = emptyList()
                    }
                }

            } catch (e: Exception) {
                Log.e("FloorMapVM", "loadAll error", e)
                _message.value = "평면도 정보를 불러오지 못했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSensorPosition(xRatio: Float, yRatio: Float) {
        val device = JetsonRepository.selectedJetson.value ?: return

        val selected = _selectedSensor.value ?: run {
            _message.value = "먼저 온습도 센서를 선택해주세요."
            return
        }

        val mapInfo = _floorMap.value ?: run {
            _message.value = "평면도 정보가 없습니다."
            return
        }

        viewModelScope.launch {
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val response = service.saveSensorPosition(
                    SaveSensorPositionRequest(
                        mapId = mapInfo.mapId,
                        sensorId = selected.sensorId,
                        xRatio = xRatio,
                        yRatio = yRatio
                    )
                )

                if (response.status == "success") {
                    _message.value = "온습도 센서 위치 저장 완료"

                    val placedResponse = service.getMapSensorPositions(mapInfo.mapId)
                    if (placedResponse.status == "success") {
                        _placedSensors.value = placedResponse.data
                    }
                } else {
                    _message.value = response.message
                }

            } catch (e: Exception) {
                Log.e("FloorMapVM", "saveSensorPosition error", e)
                _message.value = "센서 위치 저장 실패"
            }
        }
    }
}