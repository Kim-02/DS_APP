// AuthDataStore.kt
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "auth_settings")

class AuthDataStore(private val context: Context) {
    companion object {
        val DEPT_ID_KEY = stringPreferencesKey("dept_id")
        val IS_AUTO_LOGIN_KEY = booleanPreferencesKey("is_auto_login")
    }

    // 저장된 데이터를 실시간으로 읽어오는 Flow
    val authFlow: Flow<AuthState> = context.dataStore.data.map { preferences ->
        AuthState(
            deptId = preferences[DEPT_ID_KEY] ?: "",
            isAutoLogin = preferences[IS_AUTO_LOGIN_KEY] ?: false
        )
    }

    // 로그인 시 데이터 저장 함수
    suspend fun saveAuth(deptId: String, isAutoLogin: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEPT_ID_KEY] = deptId
            preferences[IS_AUTO_LOGIN_KEY] = isAutoLogin
        }
    }
}

// 상태를 담을 데이터 클래스
data class AuthState(val deptId: String, val isAutoLogin: Boolean)