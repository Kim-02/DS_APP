import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var deptId by remember { mutableStateOf("") }
    var isAutoLogin by remember { mutableStateOf(false) }

    val isLoginLoading by viewModel.isLoginLoading.collectAsState()
    val loginMessage by viewModel.loginMessage.collectAsState()
    val loginSuccessEvent by viewModel.loginSuccessEvent.collectAsState()

    LaunchedEffect(loginSuccessEvent) {
        if (loginSuccessEvent) {
            viewModel.consumeLoginSuccessEvent()
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "관리자 로그인",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = deptId,
            onValueChange = { input ->
                // 숫자만 입력 허용
                deptId = input.filter { it.isDigit() }
            },
            label = { Text("관리자 사번") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoginLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isAutoLogin,
                onCheckedChange = { isAutoLogin = it },
                enabled = !isLoginLoading
            )

            Text("자동 로그인")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loginMessage != null) {
            Text(
                text = loginMessage ?: "",
                color = if (loginMessage?.contains("성공") == true) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                if (deptId.isNotBlank()) {
                    viewModel.login(deptId, isAutoLogin)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = deptId.isNotBlank() && !isLoginLoading
        ) {
            if (isLoginLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("로그인")
            }
        }
    }
}