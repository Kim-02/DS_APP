import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ds_safer.ui.theme.OnSafeCard
import com.example.ds_safer.ui.theme.OnSafeColor
import com.example.ds_safer.ui.theme.OnSafeScreenBrush

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
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(loginSuccessEvent) {
        if (loginSuccessEvent) {
            viewModel.consumeLoginSuccessEvent()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnSafeScreenBrush)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 로고 아이콘
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = OnSafeColor.Blue.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, OnSafeColor.Blue.copy(alpha = 0.45f)),
                modifier = Modifier.size(84.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = OnSafeColor.Blue,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "ON_SAFE",
                color = OnSafeColor.TextPrimary,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "관리자 로그인",
                color = OnSafeColor.TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 입력 영역 카드
            OnSafeCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = deptId,
                    onValueChange = { deptId = it.filter { c -> c.isDigit() } },
                    label = {
                        Text(
                            "관리자 사번",
                            color = OnSafeColor.TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = OnSafeColor.Blue
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (deptId.isNotBlank() && !isLoginLoading) {
                                viewModel.login(deptId, isAutoLogin)
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoginLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OnSafeColor.Blue,
                        unfocusedBorderColor = OnSafeColor.Stroke,
                        focusedTextColor = OnSafeColor.TextPrimary,
                        unfocusedTextColor = OnSafeColor.TextPrimary,
                        cursorColor = OnSafeColor.Blue,
                        focusedContainerColor = OnSafeColor.CardSoft,
                        unfocusedContainerColor = OnSafeColor.CardSoft,
                        disabledContainerColor = OnSafeColor.CardSoft,
                        disabledBorderColor = OnSafeColor.StrokeSoft,
                        disabledTextColor = OnSafeColor.TextSecondary,
                        focusedLabelColor = OnSafeColor.Blue,
                        unfocusedLabelColor = OnSafeColor.TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isAutoLogin,
                        onCheckedChange = { isAutoLogin = it },
                        enabled = !isLoginLoading,
                        colors = CheckboxDefaults.colors(
                            checkedColor = OnSafeColor.Blue,
                            uncheckedColor = OnSafeColor.Stroke,
                            checkmarkColor = Color.White,
                            disabledCheckedColor = OnSafeColor.Blue.copy(alpha = 0.4f),
                            disabledUncheckedColor = OnSafeColor.Stroke.copy(alpha = 0.4f)
                        )
                    )
                    Text(
                        text = "자동 로그인",
                        color = OnSafeColor.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 로그인 결과 메시지
            if (loginMessage != null) {
                val isSuccess = loginMessage?.contains("성공") == true
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSuccess) OnSafeColor.Green.copy(alpha = 0.12f)
                    else OnSafeColor.Red.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.dp,
                        if (isSuccess) OnSafeColor.Green.copy(alpha = 0.4f)
                        else OnSafeColor.Red.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = loginMessage ?: "",
                        color = if (isSuccess) OnSafeColor.Green else OnSafeColor.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 로그인 버튼
            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.login(deptId, isAutoLogin)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = deptId.isNotBlank() && !isLoginLoading,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnSafeColor.Blue,
                    disabledContainerColor = OnSafeColor.Stroke,
                    contentColor = Color.White,
                    disabledContentColor = OnSafeColor.TextSecondary
                )
            ) {
                if (isLoginLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "로그인",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
