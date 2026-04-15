import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.ds_safer.data.api.RetrofitClient

class AuthViewModel(private val dataStore: AuthDataStore) : ViewModel() {

    // null: 로딩중(확인중), true: 로그인됨, false: 로그인 필요
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    // 🌟 추가: 서버에서 받아온 작업자 이름을 UI에 띄우기 위한 상태 변수
    private val _workerName = MutableStateFlow("작업자")
    val workerName = _workerName.asStateFlow()
    private val _currentDeptId = MutableStateFlow("")
    val currentDeptId = _currentDeptId.asStateFlow()

    init {
        checkAutoLogin()
    }

    private fun checkAutoLogin() {
        viewModelScope.launch {
            val authState = dataStore.authFlow.first()
            // 자동 로그인이 켜져 있고 사번이 비어있지 않으면 바로 통과
            if (authState.isAutoLogin && authState.deptId.isNotEmpty()) {
                _isLoggedIn.value = true
                _currentDeptId.value = authState.deptId // 사번 기억하기
                fetchWorkerName(authState.deptId)

                // 🌟 자동 로그인 성공 시 백그라운드에서 이름 가져오기
                fetchWorkerName(authState.deptId)
            } else {
                _isLoggedIn.value = false
            }
        }
    }

    fun login(deptId: String, isAutoLogin: Boolean) {
        viewModelScope.launch {
            // 체크박스 여부와 상관없이 일단 API 통신을 위해 사번은 저장
            dataStore.saveAuth(deptId, isAutoLogin)
            _isLoggedIn.value = true
            _currentDeptId.value = deptId // 사번 기억하기
            fetchWorkerName(deptId)

            // 🌟 로그인 버튼 누른 직후 백그라운드에서 이름 가져오기
            fetchWorkerName(deptId)
        }
    }

    // 🌟 서버(FastAPI)에 사번을 보내고 이름을 받아오는 통신 전용 함수
    private suspend fun fetchWorkerName(deptId: String) {
        try {
            // (참고) 아까 파일 합치면서 만든 createWorkerService 함수를 사용!
            val service = RetrofitClient.createWorkerService("http://192.168.0.64:8000/")
            val response = service.getWorkerName(workerId = deptId)

            // 1. 통신 성공 (200 OK) + 상태가 success 일 때
            if (response.isSuccessful && response.body()?.status == "success") {
                // 🌟 서버가 준 진짜 이름을 꺼내서 적용! (null이면 "작업자"로 세팅)
                _workerName.value = response.body()?.worker_name ?: "작업자"
            }
            // 2. 서버에서 404 (해당 사번 없음) 에러를 보냈을 때
            else if (response.code() == 404) {
                _workerName.value = "미등록 작업자"
            }
            // 3. 그 외 알 수 없는 오류
            else {
                _workerName.value = "작업자"
            }
        } catch (e: Exception) {
            // 서버가 꺼져있거나 와이파이가 끊겼을 때
            _workerName.value = "작업자"
        }
    }

    fun logout() {
        viewModelScope.launch {
            // 1. DataStore 초기화 (사번 비우고, 자동로그인 끄기)
            dataStore.saveAuth("", false)

            // 2. 상태를 false로 바꿔서 관찰 중인 UI에 알림
            _isLoggedIn.value = false

            // 🌟 로그아웃 시 화면에 남아있는 이름도 초기화
            _workerName.value = "작업자"
        }
    }
}