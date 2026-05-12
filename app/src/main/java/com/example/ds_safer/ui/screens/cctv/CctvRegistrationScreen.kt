package com.example.ds_safer.ui.screens.cctv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CctvRegistrationScreen(
    viewModel: CctvRegistrationViewModel = viewModel(),
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is CctvUiState.Success) {
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CCTV 카메라 등록",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "CCTV 자체 앱에서 Wi-Fi 설정을 완료한 뒤, 같은 네트워크에 연결된 CCTV의 IP와 비밀번호를 입력하세요.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        OutlinedTextField(
            value = viewModel.ipAddress,
            onValueChange = { viewModel.ipAddress = it },
            label = { Text("CCTV IP 주소") },
            placeholder = { Text("예: 192.168.0.50") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = viewModel.cameraPassword,
            onValueChange = { viewModel.cameraPassword = it },
            label = { Text("CCTV 비밀번호") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Text(
            text = "접속 ID는 admin으로 자동 설정됩니다.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.registerCctv() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is CctvUiState.Loading
        ) {
            if (uiState is CctvUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("CCTV 등록")
            }
        }

        when (val state = uiState) {
            is CctvUiState.Error -> {
                Text(
                    text = state.message,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            is CctvUiState.Success -> {
                Text(
                    text = "CCTV 등록이 완료되었습니다.",
                    color = Color(0xFF1976D2),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            else -> Unit
        }
    }
}