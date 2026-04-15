package com.example.ds_safer.ui.screens.cctv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.CameraCreate // 바뀐 규격의 모델 객체
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CctvRegistrationViewModel : ViewModel() {

    // --- UI 입력 상태 변수들 ---

    // 💡 참고: 서버가 다 알아서 하니까 name, ipAddress, port는 이제 API 전송용으로는 필요 없어!
    // 만약 앱 화면(UI)에서도 입력받을 필요가 없어졌다면 과감하게 지워버려도 돼.
    var name by mutableStateOf("")

    var ipAddr by mutableStateOf("")
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

                val request = CameraCreate(
                    ipAddress = ipAddr,
                    cameraId = userId,
                    cameraPw = userPw
                )

                // API 쏘기
                val response = service.registerCctv(request)

                // null 안전성 처리 및 성공 여부 판단
                if (response.message.contains("성공", ignoreCase = true)) {
                    _uiState.value = CctvUiState.Success
                } else {
                    // 성공이 아니면 에러 처리
                    _uiState.value = CctvUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = CctvUiState.Error("연결 오류: ${e.message}")
                e.printStackTrace() // 디버깅용 로그
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