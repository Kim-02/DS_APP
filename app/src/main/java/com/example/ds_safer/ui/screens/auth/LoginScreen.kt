// LoginScreen.kt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var deptId by remember { mutableStateOf("") }
    var isAutoLogin by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "관리자 로그인", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = deptId,
            onValueChange = { deptId = it },
            label = { Text("사번 (예: 1234)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isAutoLogin,
                onCheckedChange = { isAutoLogin = it }
            )
            Text("자동 로그인 (로그인 유지)")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (deptId.isNotBlank()) {
                    viewModel.login(deptId, isAutoLogin)
                    onLoginSuccess() // 완료되면 다음 화면으로 넘어감
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = deptId.isNotBlank()
        ) {
            Text("확인")
        }
    }
}