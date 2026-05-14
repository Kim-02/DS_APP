package com.example.ds_safer.ui.screens.jetson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.domain.model.JetsonAppRegisterRequest
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.domain.model.JetsonOutDto
import com.example.ds_safer.domain.model.SpaceDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JetsonSpaceRegisterViewModel : ViewModel() {

    private val _spaces = MutableStateFlow<List<SpaceDto>>(emptyList())
    val spaces = _spaces.asStateFlow()

    private val _registeredJetsons = MutableStateFlow<List<JetsonOutDto>>(emptyList())
    val registeredJetsons = _registeredJetsons.asStateFlow()

    private val _selectedJetson = MutableStateFlow<JetsonDevice?>(null)
    val selectedJetson = _selectedJetson.asStateFlow()

    private val _selectedSpace = MutableStateFlow<SpaceDto?>(null)
    val selectedSpace = _selectedSpace.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private fun createApiByJetson(jetson: JetsonDevice) =
        RetrofitClient.createService("http://${jetson.ipAddress}:${jetson.port}")

    fun selectJetson(device: JetsonDevice) {
        _selectedJetson.value = device
        _selectedSpace.value = null
        loadSpaces(device)
        loadRegisteredJetsons(device)
    }

    fun selectSpace(space: SpaceDto) {
        _selectedSpace.value = space
    }

    fun clearMessage() {
        _successMessage.value = null
        _errorMessage.value = null
    }

    fun loadSpaces(jetson: JetsonDevice? = _selectedJetson.value) {
        if (jetson == null) {
            _spaces.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val api = createApiByJetson(jetson)
                val response = api.getSpaces()

                if (response.success) {
                    _spaces.value = response.data
                } else {
                    _spaces.value = emptyList()
                    _errorMessage.value = "공간 목록 조회에 실패했습니다."
                }
            } catch (e: Exception) {
                _spaces.value = emptyList()
                _errorMessage.value = "공간 목록 조회 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRegisteredJetsons(jetson: JetsonDevice? = _selectedJetson.value) {
        if (jetson == null) {
            _registeredJetsons.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val api = createApiByJetson(jetson)
                _registeredJetsons.value = api.getRegisteredJetsonsV1()
            } catch (e: Exception) {
                _registeredJetsons.value = emptyList()
                _errorMessage.value = "등록된 Jetson 목록 조회 실패: ${e.message}"
            }
        }
    }

    fun registerSelectedJetson(onSuccess: (() -> Unit)? = null) {
        val jetson = _selectedJetson.value
        val space = _selectedSpace.value

        if (jetson == null) {
            _errorMessage.value = "등록할 Jetson을 선택해주세요."
            return
        }

        if (space == null) {
            _errorMessage.value = "등록할 공간을 선택해주세요."
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _successMessage.value = null

                val api = createApiByJetson(jetson)

                val response = api.registerJetsonFromMdns(
                    JetsonAppRegisterRequest(
                        jetsonWp = jetson.name,
                        jetsonLoc = jetson.name,
                        jetsonStatus = true,
                        ipAddr = jetson.ipAddress,
                        port = jetson.port,
                        spaceId = space.spaceId
                    )
                )

                if (response.success) {
                    _successMessage.value =
                        response.data?.spaceName?.let {
                            "Jetson이 $it 에 등록되었습니다."
                        } ?: response.message

                    loadRegisteredJetsons(jetson)
                    onSuccess?.invoke()
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "Jetson 등록 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteJetson(jetsonId: Int, onSuccess: (() -> Unit)? = null) {
        val jetson = _selectedJetson.value

        if (jetson == null) {
            _errorMessage.value = "Jetson 서버가 선택되지 않았습니다."
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _successMessage.value = null

                val api = createApiByJetson(jetson)
                val response = api.deleteJetsonV1(jetsonId)

                if (response.success) {
                    _successMessage.value = response.message.ifBlank {
                        "Jetson 등록이 해제되었습니다."
                    }

                    loadRegisteredJetsons(jetson)
                    onSuccess?.invoke()
                } else {
                    _errorMessage.value = response.message.ifBlank {
                        "Jetson 삭제에 실패했습니다."
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Jetson 삭제 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}