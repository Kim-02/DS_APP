package com.example.ds_safer.ui.screens.cctv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.ui.theme.OnSafeCard
import com.example.ds_safer.ui.theme.OnSafeColor
import com.example.ds_safer.ui.theme.OnSafePrimaryButton
import com.example.ds_safer.ui.theme.OnSafeScreenBrush
import com.example.ds_safer.ui.theme.OnSafeSectionTitle
import com.example.ds_safer.ui.theme.OnSafeStatusDot

@Composable
fun CctvRegistrationScreen(
    viewModel: CctvRegistrationViewModel = viewModel(),
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val demoUiState by viewModel.demoUiState.collectAsState()
    val selectedJetson by JetsonRepository.selectedJetson.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is CctvUiState.Success) {
            onSuccess()
        }
    }

    LaunchedEffect(demoUiState) {
        if (demoUiState is CctvUiState.Success) {
            onSuccess()
        }
    }

    Scaffold(
        containerColor = OnSafeColor.BgTop,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OnSafeColor.CardDark)
                    .systemBarsPadding()
                    .padding(16.dp)
            ) {
                OnSafePrimaryButton(
                    text = if (uiState is CctvUiState.Loading) "검증 중..." else "CCTV 등록하기",
                    enabled = uiState !is CctvUiState.Loading,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.registerCctv()
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(OnSafeScreenBrush)
                .padding(padding)
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSuccess) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = OnSafeColor.TextPrimary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CCTV 등록",
                            color = OnSafeColor.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "IP와 비밀번호를 입력해 CCTV를 검증 후 등록합니다.",
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                OnSafeCard(
                    modifier = Modifier.fillMaxWidth(),
                    selected = true
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(OnSafeColor.Blue.copy(alpha = 0.18f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = OnSafeColor.Blue
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedJetson?.spaceName ?: "공간 미지정",
                                color = OnSafeColor.TextPrimary,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = selectedJetson?.let {
                                    "${it.name} · ${it.ipAddress}:${it.port}"
                                } ?: "선택된 Jetson이 없습니다.",
                                color = OnSafeColor.TextSecondary,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OnSafeStatusDot(
                                color = if (selectedJetson != null) OnSafeColor.Green else OnSafeColor.Red,
                                text = if (selectedJetson != null) "등록 대상 확인됨" else "Jetson 선택 필요"
                            )
                        }
                    }
                }
            }

            item {
                OnSafeSectionTitle("CCTV 연결 정보")
            }

            item {
                OnSafeCard(modifier = Modifier.fillMaxWidth()) {
                    DarkOutlinedField(
                        value = viewModel.ipAddress,
                        onValueChange = { viewModel.ipAddress = it },
                        label = "CCTV IP 주소",
                        placeholder = "예: 192.168.0.50",
                        icon = Icons.Default.Info,
                        keyboardType = KeyboardType.Uri
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DarkOutlinedField(
                        value = viewModel.cameraPassword,
                        onValueChange = { viewModel.cameraPassword = it },
                        label = "CCTV 비밀번호",
                        placeholder = "카메라 비밀번호 입력",
                        icon = Icons.Default.Info,
                        keyboardType = KeyboardType.Password,
                        password = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = OnSafeColor.Blue.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, OnSafeColor.Blue.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = OnSafeColor.Blue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "등록 전 연결 검증",
                                    color = OnSafeColor.TextPrimary,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "서버에서 CCTV 연결 가능 여부를 확인한 뒤, 검증 성공 시에만 DB에 저장됩니다.",
                                color = OnSafeColor.TextSecondary,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "접속 ID는 admin으로 자동 설정됩니다.",
                                color = OnSafeColor.TextTertiary,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                when (val state = uiState) {
                    is CctvUiState.Idle -> {
                        StatusMessage(
                            message = "CCTV IP와 비밀번호를 입력한 뒤 등록을 진행하세요.",
                            color = OnSafeColor.TextSecondary,
                            icon = Icons.Default.Info
                        )
                    }

                    is CctvUiState.Loading -> {
                        LoadingMessage()
                    }

                    is CctvUiState.Success -> {
                        StatusMessage(
                            message = "CCTV 등록이 완료되었습니다.",
                            color = OnSafeColor.Green,
                            icon = Icons.Default.Check
                        )
                    }

                    is CctvUiState.Error -> {
                        StatusMessage(
                            message = state.message,
                            color = OnSafeColor.Red,
                            icon = Icons.Default.Warning
                        )
                    }
                }
            }

            item {
                DemoCctvRegisterCard(
                    demoState = demoUiState,
                    onRegisterClick = { viewModel.registerDemoCamera() }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun DarkOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    password: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = {
            Text(label)
        },
        placeholder = {
            Text(placeholder)
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        visualTransformation = if (password) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = OnSafeColor.TextPrimary,
            unfocusedTextColor = OnSafeColor.TextPrimary,
            focusedBorderColor = OnSafeColor.Blue,
            unfocusedBorderColor = OnSafeColor.Stroke,
            focusedLabelColor = OnSafeColor.Blue,
            unfocusedLabelColor = OnSafeColor.TextSecondary,
            cursorColor = OnSafeColor.Blue,
            focusedLeadingIconColor = OnSafeColor.Blue,
            unfocusedLeadingIconColor = OnSafeColor.TextSecondary,
            focusedPlaceholderColor = OnSafeColor.TextTertiary,
            unfocusedPlaceholderColor = OnSafeColor.TextTertiary
        )
    )
}

@Composable
private fun LoadingMessage() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = OnSafeColor.Card,
        border = BorderStroke(1.dp, OnSafeColor.Stroke)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = OnSafeColor.Blue
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "CCTV 연결을 검증하고 등록하는 중입니다.",
                color = OnSafeColor.TextSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DemoCctvRegisterCard(
    demoState: CctvUiState,
    onRegisterClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(8.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = OnSafeColor.Red.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, OnSafeColor.Red.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(OnSafeColor.Red.copy(alpha = 0.14f), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = OnSafeColor.Red,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "시연용 CCTV 등록",
                        color = OnSafeColor.Red,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "사전에 준비된 화재 영상을 사용하는 가상 CCTV입니다.",
                        color = OnSafeColor.TextSecondary,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (demoState) {
                is CctvUiState.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = OnSafeColor.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "등록 중...",
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }
                }
                is CctvUiState.Error -> {
                    Text(
                        text = demoState.message,
                        color = OnSafeColor.Red,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DemoRegisterButton(enabled = true, onClick = onRegisterClick)
                }
                else -> {
                    DemoRegisterButton(
                        enabled = demoState !is CctvUiState.Loading,
                        onClick = onRegisterClick
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoRegisterButton(enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) OnSafeColor.Red else OnSafeColor.Gray,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "시연용 CCTV 등록",
                color = androidx.compose.ui.graphics.Color.White,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun StatusMessage(
    message: String,
    color: Color,
    icon: ImageVector
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (color == OnSafeColor.TextSecondary) {
            OnSafeColor.Card
        } else {
            color.copy(alpha = 0.12f)
        },
        border = BorderStroke(
            1.dp,
            if (color == OnSafeColor.TextSecondary) {
                OnSafeColor.Stroke
            } else {
                color.copy(alpha = 0.4f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = message,
                color = color,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }
}