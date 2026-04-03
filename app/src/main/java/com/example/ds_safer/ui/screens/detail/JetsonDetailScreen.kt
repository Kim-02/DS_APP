package com.example.ds_safer.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ds_safer.domain.model.JetsonDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JetsonDetailScreen(
    device: JetsonDevice,
    onDisconnectClick: () -> Unit,             // 1. 연결 해제 누를 때
    onNavigateToSensorRegister: () -> Unit,    // 2. 센서 등록 누를 때
    onNavigateToCctvRegister: () -> Unit,      // 3. CCTV 등록 누를 때
    onBackClick: () -> Unit                    // 뒤로 가기
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device.name) },
                navigationIcon = {
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 젯슨 정보 요약 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("IP 주소: ${device.ipAddress}", style = MaterialTheme.typography.bodyLarge)
                    Text("포트: ${device.port}", style = MaterialTheme.typography.bodyLarge)
                    Text("기기 ID: ${device.jetsonId}", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 1. 센서 등록 버튼
            MenuButton(
                icon = Icons.Default.Build,
                text = "센서 등록하기 (다중 선택)",
                onClick = onNavigateToSensorRegister
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. CCTV 등록 버튼
            MenuButton(
                icon = Icons.Default.Add,
                text = "CCTV 등록하기",
                onClick = onNavigateToCctvRegister
            )

            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(32.dp))

            // 3. 젯슨 연결 해제 버튼 (위험하니까 빨간색 톤으로)
            Button(
                onClick = onDisconnectClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("이 젯슨 기기 연결 해제", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// 메뉴 버튼용 공통 컴포저블
@Composable
fun MenuButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}