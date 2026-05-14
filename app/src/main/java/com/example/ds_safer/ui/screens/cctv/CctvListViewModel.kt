package com.example.ds_safer.ui.screens.cctv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.CameraOutResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CctvListViewModel : ViewModel() {

    private val _cameraList = MutableStateFlow<List<CameraOutResponse>>(emptyList())
    val cameraList = _cameraList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private fun createServiceOrNull(): com.example.ds_safer.data.api.JetsonApiService? {
        val selectedJetson = JetsonRepository.selectedJetson.value

        if (selectedJetson == null) {
            _errorMessage.value = "선택된 Jetson이 없습니다."
            return null
        }

        return RetrofitClient.createService(
            "http://${selectedJetson.ipAddress}:${selectedJetson.port}/"
        )
    }

    fun fetchCameras() {
        val selectedJetson = JetsonRepository.selectedJetson.value

        if (selectedJetson == null) {
            _cameraList.value = emptyList()
            _errorMessage.value = "선택된 Jetson이 없습니다."
            return
        }

        val spaceId = selectedJetson.spaceId
        if (spaceId == null || spaceId <= 0) {
            _cameraList.value = emptyList()
            _errorMessage.value = "현재 Jetson의 공간 정보가 없습니다."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val service = RetrofitClient.createService(
                    "http://${selectedJetson.ipAddress}:${selectedJetson.port}/"
                )

                val response = service.getCameras(spaceId = spaceId)
                _cameraList.value = response
            } catch (e: Exception) {
                e.printStackTrace()
                _cameraList.value = emptyList()
                _errorMessage.value = e.message ?: "CCTV 목록 조회 중 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCamera(cameraId: Int) {
        val selectedJetson = JetsonRepository.selectedJetson.value

        if (selectedJetson == null) {
            _errorMessage.value = "선택된 Jetson이 없습니다."
            return
        }

        val spaceId = selectedJetson.spaceId

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val service = RetrofitClient.createService(
                    "http://${selectedJetson.ipAddress}:${selectedJetson.port}/"
                )

                val deleteResponse = service.deleteCamera(cameraId)

                if (!deleteResponse.isSuccessful) {
                    val body = deleteResponse.errorBody()?.string()
                    _errorMessage.value = body ?: "CCTV 삭제에 실패했습니다. (${deleteResponse.code()})"
                    return@launch
                }

                val updated = service.getCameras(spaceId = spaceId)
                _cameraList.value = updated
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = e.message ?: "CCTV 삭제 중 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }
}