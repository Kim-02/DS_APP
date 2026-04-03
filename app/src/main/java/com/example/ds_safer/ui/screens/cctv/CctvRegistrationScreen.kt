package com.example.ds_safer.ui.screens.cctv

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
        if (uiState is CctvUiState.Success) onSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("CCTV 카메라 등록", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = viewModel.ipAddr,
            onValueChange = {viewModel.ipAddr = it},
            label = {Text("윤정아")},
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.userId,
            onValueChange = { viewModel.userId = it },
            label = { Text("접속 ID") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.userPw,
            onValueChange = { viewModel.userPw = it },
            label = { Text("접속 비밀번호") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.registerCctv() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is CctvUiState.Loading
        ) {
            if (uiState is CctvUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("CCTV 등록 요청")
            }
        }

        if (uiState is CctvUiState.Error) {
            Text(
                text = (uiState as CctvUiState.Error).message,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}