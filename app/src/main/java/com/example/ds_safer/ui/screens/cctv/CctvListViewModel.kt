package com.example.ds_safer.ui.screens.cctv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.RegisteredCamera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CctvListViewModel : ViewModel() {
    private val _cameraList = MutableStateFlow<List<RegisteredCamera>>(emptyList())
    val cameraList = _cameraList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchCameras() {
        val device = JetsonRepository.selectedJetson.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                // API 호출해서 껍데기 통째로 받기
                val response = service.getRegisteredCameras()

                // status가 "success"일 때만 안의 data(리스트)를 꺼내서 뷰모델에 저장!
                if (response.status == "success") {
                    _cameraList.value = response.data
                } else {
                    _cameraList.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _cameraList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}