package com.example.ds_safer.ui.screens.detail

import android.content.Intent // Intent 임포트 추가!
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info // 아이콘 임포트 추가
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext // LocalContext 임포트 추가!
import androidx.compose.ui.unit.dp
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.ui.screens.monitor.MonitoringActivity // 아까 만든 화면 임포트!

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JetsonDetailScreen(
    device: JetsonDevice,
    onDisconnectClick: () -> Unit,
    onNavigateToSensorRegister: () -> Unit,
    onNavigateToCctvRegister: () -> Unit,
    onBackClick: () -> Unit
) {
    // 🌟 Compose 화면에서 Activity를 띄우기 위해 Context를 가져옵니다.
    val context = LocalContext.current

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

            Spacer(modifier = Modifier.height(16.dp))

            // 👇 3. 새로 추가하는 현황 모니터링 버튼! 👇
            MenuButton(
                icon = Icons.Default.Info, // 적당한 아이콘 (Info)
                text = "실시간 현황 모니터링",
                onClick = {
                    // Compose 환경에서 기존 방식의 Activity 호출하기
                    val intent = Intent(context, MonitoringActivity::class.java)
                    // 파라미터로 받은 device 객체에서 진짜 IP 주소를 꺼내서 넘겨줍니다!
                    intent.putExtra("JETSON_IP", device.ipAddress)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(32.dp))

            // 젯슨 연결 해제 버튼 (위험하니까 빨간색 톤으로)
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

// 메뉴 버튼용 공통 컴포저블 (이 부분은 수정 없음!)
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