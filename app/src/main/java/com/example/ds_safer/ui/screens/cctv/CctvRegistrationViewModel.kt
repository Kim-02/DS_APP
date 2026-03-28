package com.example.ds_safer.ui.screens.cctv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.CctvRegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CctvRegistrationViewModel : ViewModel() { // <-- 반드시 ViewModel()을 상속해야 합니다!

    // 상태 변수들 (getValue, setValue 임포트 필수)
    var name by mutableStateOf("")
    var ipAddress by mutableStateOf("")
    var port by mutableStateOf("554")
    var userId by mutableStateOf("")
    var userPw by mutableStateOf("")

    private val _uiState = MutableStateFlow<CctvUiState>(CctvUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun registerCctv() {
        val device = JetsonRepository.selectedJetson.value ?: return

        viewModelScope.launch {
            _uiState.value = CctvUiState.Loading
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val request = CctvRegisterRequest(name, ipAddress, port.toInt(), userId, userPw)
                val response = service.registerCctv(request)

                if (response.status == "success") {
                    _uiState.value = CctvUiState.Success
                } else {
                    _uiState.value = CctvUiState.Error(response.message ?: "등록 실패")
                }
            } catch (e: Exception) {
                _uiState.value = CctvUiState.Error("연결 오류: ${e.message}")
            }
        }
    }
}

sealed class CctvUiState {
    object Idle : CctvUiState()
    object Loading : CctvUiState()
    object Success : CctvUiState()
    data class Error(val message: String) : CctvUiState()
}