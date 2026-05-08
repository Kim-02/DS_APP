import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.domain.model.WorkerDbResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(private val dataStore: AuthDataStore) : ViewModel() {

    // null: 로딩중, true: 로그인됨, false: 로그인 필요
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _workerName = MutableStateFlow("작업자")
    val workerName = _workerName.asStateFlow()

    private val _currentDeptId = MutableStateFlow("")
    val currentDeptId = _currentDeptId.asStateFlow()

    private val _isLoginLoading = MutableStateFlow(false)
    val isLoginLoading = _isLoginLoading.asStateFlow()

    private val _loginMessage = MutableStateFlow<String?>(null)
    val loginMessage = _loginMessage.asStateFlow()

    // LoginScreen에서 이 값이 true가 되면 onLoginSuccess() 호출
    private val _loginSuccessEvent = MutableStateFlow(false)
    val loginSuccessEvent = _loginSuccessEvent.asStateFlow()

    // TODO: 추후 Jetson 자동 탐색 결과를 쓰도록 변경 가능
    // 현재 API 서버는 8080, VLM은 8000
    private val apiBaseUrl = "http://192.168.0.66:8080/"

    init {
        checkAutoLogin()
    }

    private fun checkAutoLogin() {
        viewModelScope.launch {
            val authState = dataStore.authFlow.first()

            if (authState.isAutoLogin && authState.deptId.isNotEmpty()) {
                // 자동 로그인도 반드시 서버에서 관리자 여부를 다시 확인
                validateSavedLogin(authState.deptId)
            } else {
                _isLoggedIn.value = false
            }
        }
    }

    private suspend fun validateSavedLogin(deptId: String) {
        val deptIdInt = deptId.toIntOrNull()

        if (deptIdInt == null) {
            clearInvalidLogin("저장된 사번 형식이 올바르지 않습니다.")
            return
        }

        try {
            val worker = fetchWorker(deptIdInt)

            if (worker.isManager == 1) {
                _workerName.value = worker.name
                _currentDeptId.value = worker.deptId.toString()
                _isLoggedIn.value = true
            } else {
                clearInvalidLogin("관리자 계정이 아닙니다.")
            }

        } catch (e: Exception) {
            clearInvalidLogin("자동 로그인 검증 실패: ${e.message}")
        }
    }

    fun login(deptId: String, isAutoLogin: Boolean) {
        viewModelScope.launch {
            _isLoginLoading.value = true
            _loginMessage.value = null
            _loginSuccessEvent.value = false

            val deptIdInt = deptId.toIntOrNull()

            if (deptIdInt == null) {
                _loginMessage.value = "사번은 숫자로 입력하세요."
                _isLoginLoading.value = false
                return@launch
            }

            try {
                val worker = fetchWorker(deptIdInt)

                if (worker.isManager != 1) {
                    _loginMessage.value = "관리자 계정만 로그인할 수 있습니다."
                    _isLoggedIn.value = false
                    return@launch
                }

                dataStore.saveAuth(worker.deptId.toString(), isAutoLogin)

                _workerName.value = worker.name
                _currentDeptId.value = worker.deptId.toString()
                _isLoggedIn.value = true
                _loginMessage.value = "${worker.name} 관리자님 로그인 성공"
                _loginSuccessEvent.value = true

            } catch (e: retrofit2.HttpException) {
                _isLoggedIn.value = false

                _loginMessage.value = when (e.code()) {
                    404 -> "등록되지 않은 사번입니다."
                    403 -> "관리자 권한이 없습니다."
                    else -> "로그인 실패: HTTP ${e.code()}"
                }

            } catch (e: Exception) {
                _isLoggedIn.value = false
                _loginMessage.value = "로그인 실패: ${e.message}"

            } finally {
                _isLoginLoading.value = false
            }
        }
    }

    private suspend fun fetchWorker(deptId: Int): WorkerDbResponse {
        val service = RetrofitClient.createService(apiBaseUrl)
        return service.getDbWorker(deptId)
    }

    private suspend fun clearInvalidLogin(message: String) {
        dataStore.saveAuth("", false)
        _workerName.value = "작업자"
        _currentDeptId.value = ""
        _isLoggedIn.value = false
        _loginMessage.value = message
    }

    fun consumeLoginSuccessEvent() {
        _loginSuccessEvent.value = false
    }

    fun consumeLoginMessage() {
        _loginMessage.value = null
    }

    fun logout() {
        viewModelScope.launch {
            dataStore.saveAuth("", false)

            _isLoggedIn.value = false
            _workerName.value = "작업자"
            _currentDeptId.value = ""
            _loginMessage.value = null
            _loginSuccessEvent.value = false
        }
    }
}