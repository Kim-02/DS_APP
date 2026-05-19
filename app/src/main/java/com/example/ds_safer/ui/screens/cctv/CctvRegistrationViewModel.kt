package com.example.ds_safer.ui.screens.cctv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.AppCameraRegisterRequest
import com.example.ds_safer.domain.model.RegisterDemoCctvRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CctvRegistrationViewModel : ViewModel() {

    var ipAddress by mutableStateOf("")
    var cameraPassword by mutableStateOf("")

    private val _uiState = MutableStateFlow<CctvUiState>(CctvUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _demoUiState = MutableStateFlow<CctvUiState>(CctvUiState.Idle)
    val demoUiState = _demoUiState.asStateFlow()

    fun registerCctv() {
        val selectedJetson = JetsonRepository.selectedJetson.value

        if (selectedJetson == null) {
            _uiState.value = CctvUiState.Error("선택된 Jetson이 없습니다.")
            return
        }

        val jetsonId = selectedJetson.jetsonId
        if (jetsonId == null || jetsonId <= 0) {
            _uiState.value = CctvUiState.Error("DB에 등록된 Jetson 정보가 없습니다. Jetson을 공간에 먼저 등록해주세요.")
            return
        }

        val spaceId = selectedJetson.spaceId
        if (spaceId == null || spaceId <= 0) {
            _uiState.value = CctvUiState.Error("현재 Jetson에 등록된 공간 정보가 없습니다. Jetson을 다시 등록해주세요.")
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
                    spaceId = spaceId,
                    jetsonId = jetsonId,
                    rtspPath = null
                )

                service.registerCctv(request)

                _uiState.value = CctvUiState.Success
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val message = when (e.code()) {
                    400, 404, 409, 422 -> {
                        extractDetail(errorBody)
                            ?: "CCTV 등록 실패: IP, ID, 비밀번호 또는 RTSP 경로를 확인해주세요."
                    }

                    else -> {
                        extractDetail(errorBody)
                            ?: "CCTV 등록 중 서버 오류가 발생했습니다. (${e.code()})"
                    }
                }

                _uiState.value = CctvUiState.Error(message)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = CctvUiState.Error(
                    e.message ?: "CCTV 등록 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun registerDemoCamera() {
        val selectedJetson = JetsonRepository.selectedJetson.value
        if (selectedJetson == null) {
            _demoUiState.value = CctvUiState.Error("선택된 Jetson이 없습니다.")
            return
        }
        val spaceId = selectedJetson.spaceId
        if (spaceId == null || spaceId <= 0) {
            _demoUiState.value = CctvUiState.Error("현재 Jetson에 등록된 공간 정보가 없습니다.")
            return
        }

        viewModelScope.launch {
            _demoUiState.value = CctvUiState.Loading
            try {
                val baseUrl = "http://${selectedJetson.ipAddress}:${selectedJetson.port}/"
                val service = RetrofitClient.createService(baseUrl)

                val response = service.registerDemoCctv(
                    RegisterDemoCctvRequest(
                        spaceId = spaceId,
                        jetsonId = selectedJetson.jetsonId,
                        name = "시연용 화재 CCTV",
                        demoVideoKey = "scenario3_fire",
                    )
                )
                if (response.success) {
                    _demoUiState.value = CctvUiState.Success
                } else {
                    _demoUiState.value = CctvUiState.Error(
                        response.message ?: "시연용 CCTV 등록에 실패했습니다."
                    )
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _demoUiState.value = CctvUiState.Error(
                    extractDetail(errorBody) ?: "시연용 CCTV 등록 실패 (${e.code()})"
                )
            } catch (e: Exception) {
                _demoUiState.value = CctvUiState.Error(
                    e.message ?: "시연용 CCTV 등록 중 오류가 발생했습니다."
                )
            }
        }
    }

    companion object {
        private const val DEFAULT_CAMERA_USERNAME = "admin"

        private fun extractDetail(errorBody: String?): String? {
            if (errorBody.isNullOrBlank()) return null

            return try {
                val regex = Regex("\"detail\"\\s*:\\s*\"([^\"]+)\"")
                regex.find(errorBody)?.groupValues?.get(1)
            } catch (_: Exception) {
                null
            }
        }
    }
}

sealed class CctvUiState {
    data object Idle : CctvUiState()
    data object Loading : CctvUiState()
    data object Success : CctvUiState()
    data class Error(val message: String) : CctvUiState()
}