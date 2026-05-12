package com.example.ds_safer.ui.screens.cctv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.AppCameraRegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CctvRegistrationViewModel : ViewModel() {

    var ipAddress by mutableStateOf("")
    var cameraPassword by mutableStateOf("")

    private val _uiState = MutableStateFlow<CctvUiState>(CctvUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun registerCctv() {
        val selectedJetson = JetsonRepository.selectedJetson.value

        if (selectedJetson == null) {
            _uiState.value = CctvUiState.Error("선택된 Jetson이 없습니다.")
            return
        }

        val trimmedIp = ipAddress.trim()
        val trimmedPassword = cameraPassword.trim()

        if (trimmedIp.isBlank()) {
            _uiState.value = CctvUiState.Error("CCTV IP 주소를 입력하세요.")
            return
        }

        if (trimmedPassword.isBlank()) {
            _uiState.value = CctvUiState.Error("CCTV 비밀번호를 입력하세요.")
            return
        }

        viewModelScope.launch {
            _uiState.value = CctvUiState.Loading

            try {
                val baseUrl = "http://${selectedJetson.ipAddress}:${selectedJetson.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val request = AppCameraRegisterRequest(
                    ipAddress = trimmedIp,
                    cameraUsername = DEFAULT_CAMERA_USERNAME,
                    cameraPassword = trimmedPassword,
                    name = "CCTV-$trimmedIp",
                    processId = DEFAULT_PROCESS_ID,
                    rtspPath = null
                )

                service.registerCctv(request)

                _uiState.value = CctvUiState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = CctvUiState.Error(
                    e.message ?: "CCTV 등록 중 오류가 발생했습니다."
                )
            }
        }
    }

    companion object {
        private const val DEFAULT_CAMERA_USERNAME = "admin"

        /*
         * 중요:
         * 현재 Python CCTV 모듈의 AppCameraRegisterReq는 process_id를 필수로 요구합니다.
         * 앱에서 공정 선택 기능이 아직 없다면 일단 기본값 1을 사용합니다.
         *
         * 나중에 process 목록/선택 API가 생기면 이 값을 선택된 process_id로 교체하세요.
         */
        private const val DEFAULT_PROCESS_ID = 1
    }
}

sealed class CctvUiState {
    data object Idle : CctvUiState()
    data object Loading : CctvUiState()
    data object Success : CctvUiState()
    data class Error(val message: String) : CctvUiState()
}