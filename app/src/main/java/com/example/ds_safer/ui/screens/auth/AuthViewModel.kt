// AuthViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(private val dataStore: AuthDataStore) : ViewModel() {

    // null: 로딩중(확인중), true: 로그인됨, false: 로그인 필요
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        checkAutoLogin()
    }

    private fun checkAutoLogin() {
        viewModelScope.launch {
            val authState = dataStore.authFlow.first()
            // 자동 로그인이 켜져 있고 사번이 비어있지 않으면 바로 통과
            if (authState.isAutoLogin && authState.deptId.isNotEmpty()) {
                _isLoggedIn.value = true
            } else {
                _isLoggedIn.value = false
            }
        }
    }

    fun login(deptId: String, isAutoLogin: Boolean) {
        viewModelScope.launch {
            // 체크박스 여부와 상관없이 일단 API 통신을 위해 사번은 저장 (체크 안했으면 다음번엔 무시됨)
            dataStore.saveAuth(deptId, isAutoLogin)
            _isLoggedIn.value = true
        }
    }

    fun logout() {
        viewModelScope.launch {
            // 1. DataStore 초기화 (사번 비우고, 자동로그인 끄기)
            dataStore.saveAuth("", false)

            // 2. 상태를 false로 바꿔서 관찰 중인 UI에 알림
            _isLoggedIn.value = false
        }
    }
}