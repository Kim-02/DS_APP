package com.example.ds_safer.ui.screens.sensor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.AssignHeartBandRequest
import com.example.ds_safer.domain.model.DiscoveredSensor
import com.example.ds_safer.domain.model.SensorRegisterRequest
import com.example.ds_safer.domain.model.WorkerDbResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SensorRegistrationViewModel : ViewModel() {

    private val _readySensors = MutableStateFlow<List<DiscoveredSensor>>(emptyList())
    val readySensors = _readySensors.asStateFlow()

    private val _workers = MutableStateFlow<List<WorkerDbResponse>>(emptyList())
    val workers = _workers.asStateFlow()

    private val _selectedSensor = MutableStateFlow<DiscoveredSensor?>(null)
    val selectedSensor = _selectedSensor.asStateFlow()

    private val _selectedWorker = MutableStateFlow<WorkerDbResponse?>(null)
    val selectedWorker = _selectedWorker.asStateFlow()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess = _registerSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    init {
        fetchInitialData()
    }

    fun fetchInitialData() {
        fetchReadySensors()
        fetchWorkers()
    }

    fun fetchReadySensors() {
        val device = JetsonRepository.selectedJetson.value ?: return

        viewModelScope.launch {
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val response = service.getDiscoverSensors()

                _readySensors.value = response.data
            } catch (e: Exception) {
                _readySensors.value = emptyList()
                _message.value = "발견 센서 조회 실패: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun fetchWorkers() {
        val device = JetsonRepository.selectedJetson.value ?: return

        viewModelScope.launch {
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                // is_manager=0: 작업자만 조회
                val response = service.getDbWorkers(isManager = 0)

                _workers.value = response
            } catch (e: Exception) {
                _workers.value = emptyList()
                _message.value = "작업자 조회 실패: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun selectSensor(sensor: DiscoveredSensor) {
        if (sensor.sensorType != "heart_band") {
            _message.value = "작업자에게는 heart_band 워치만 매핑할 수 있습니다."
            return
        }

        _selectedSensor.value = sensor
        _message.value = null
    }

    fun selectWorker(worker: WorkerDbResponse) {
        if (worker.isManager == 1) {
            _message.value = "관리자에게는 워치를 매핑할 수 없습니다."
            return
        }

        if (worker.senId != null) {
            _message.value = "이미 센서가 배정된 작업자입니다."
            return
        }

        _selectedWorker.value = worker
        _message.value = null
    }

    fun assignSelectedWatchToWorker() {
        val sensor = _selectedSensor.value
        val worker = _selectedWorker.value

        if (sensor == null) {
            _message.value = "등록할 워치를 선택하세요."
            return
        }

        if (worker == null) {
            _message.value = "워치를 배정할 작업자를 선택하세요."
            return
        }

        if (sensor.sensorType != "heart_band") {
            _message.value = "heart_band 타입만 작업자에게 매핑할 수 있습니다."
            return
        }

        val device = JetsonRepository.selectedJetson.value ?: run {
            _message.value = "선택된 Jetson 정보가 없습니다."
            return
        }

        val actualJetsonId = device.jetsonId

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val request = AssignHeartBandRequest(
                    sensorId = sensor.sensorId,
                    jetsonId = actualJetsonId,
                    intervalMs = 5000
                )

                val response = service.assignHeartBandToWorker(
                    deptId = worker.deptId,
                    request = request
                )

                if (response.success) {
                    _message.value = response.message
                    _registerSuccess.value = true

                    _selectedSensor.value = null
                    _selectedWorker.value = null

                    fetchReadySensors()
                    fetchWorkers()
                } else {
                    _message.value = response.message
                }

            } catch (e: Exception) {
                _message.value = "워치 등록 및 작업자 매핑 실패: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 기존 다중 센서 등록 기능은 온습도 센서 등 일반 센서 등록용으로 남겨둔다.
     * 단, heart_band는 작업자 매핑 API를 사용하는 것이 원칙이다.
     */
    fun registerMultipleSensors(selectedList: List<DiscoveredSensor>) {
        if (selectedList.isEmpty()) return

        val heartBandIncluded = selectedList.any { it.sensorType == "heart_band" }
        if (heartBandIncluded) {
            _message.value = "heart_band 워치는 작업자를 선택해서 등록해야 합니다."
            return
        }

        val device = JetsonRepository.selectedJetson.value ?: return
        val actualJetsonId = device.jetsonId ?: 1

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val requestBody = SensorRegisterRequest(
                    jetsonId = "jetson-$actualJetsonId",
                    selectedSensors = selectedList
                )

                val response = service.registerSensor(requestBody)

                if (response.status == "success") {
                    _message.value = response.message
                    _registerSuccess.value = true
                    fetchReadySensors()
                } else {
                    _message.value = response.message
                }

            } catch (e: Exception) {
                _message.value = "센서 등록 실패: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun consumeRegisterSuccess() {
        _registerSuccess.value = false
    }

    fun consumeMessage() {
        _message.value = null
    }
}