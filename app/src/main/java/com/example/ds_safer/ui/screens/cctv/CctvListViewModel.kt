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

    fun fetchCameras() {
        val selectedJetson = JetsonRepository.selectedJetson.value

        if (selectedJetson == null) {
            _cameraList.value = emptyList()
            _errorMessage.value = "선택된 Jetson이 없습니다."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val baseUrl = "http://${selectedJetson.ipAddress}:${selectedJetson.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val response = service.getCameras()

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
}