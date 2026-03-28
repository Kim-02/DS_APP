package com.example.ds_safer.ui.screens.discovery

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.JetsonDevice

@Composable
fun DiscoveryScreen(
    navController: NavHostController,
    // Hilt 사용 안 할 시 Factory 설정 필요할 수 있음
    viewModel: DiscoveryViewModel = viewModel()
) {
    // ViewModel 상태 구독
    val devices by viewModel.discoveredDevices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {viewModel.startScanning() },
            enabled = !isScanning,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isScanning) {
                Text("주변 Jetson 탐색 중")
            } else {
                Text("젯슨 등록하기")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DiscoveryScreen.kt 내부의 LazyColumn 부분
        LazyColumn(modifier = Modifier.weight(1f)) {
            // 1. 기기를 찾았을 때
            if (devices.isNotEmpty()) {
                items(devices) { device ->
                    JetsonDeviceItem(device) {
                        if (device.isConnected) {
                            // 저장소에 젯슨 정보 저장
                            JetsonRepository.setSelectedJetson(device)

                            // 메인 대시보드로 돌아가기
                            navController.popBackStack()

                            Log.d("NAV","젯슨 등록 및 대시보드 복귀: ${device.ipAddress}")
                        }
                    }
                }
            } else {
                // 2. 아직 아무것도 못 찾았을 때 (사용자님이 보고 싶어 하시는 부분)
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanning){
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("주변의 젯슨 기기를 탐색하고 있습니다...", color = Color.Gray)
                                Text("같은 Wi-Fi에 연결되어 있는지 확인해 주세요.", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Text("주변에 발견된 Jetson이 없음", textAlign = TextAlign.Center, color = Color.Gray)
                        }

                    }
                }
            }
        }
    }
    }

// 젯슨 정보를 보여주는 카드 컴포넌트
@Composable
fun JetsonDeviceItem(
    device: JetsonDevice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // 카드 간격 확보
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. 젯슨 이름 (ID 역할)
            Text(
                text = "기기 이름: ${device.name}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. IP 주소 및 포트 정보 출력
            Text(text = "IP 주소: ${device.ipAddress}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "포트: ${device.port}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // 3. 실시간 연결 상태 표시
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusText = if (device.isConnected) "✅ 연결 확인됨 (Health OK)" else "❌ 연결 확인 중..."
                val statusColor = if (device.isConnected) Color(0xFF4CAF50) else Color.Red

                Text(
                    text = statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatusIndicator(isConnected: Boolean) {
    val color = if (isConnected) Color.Green else Color.Red
    val statusText = if (isConnected) "연결됨" else "연결안됨"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = statusText, style = MaterialTheme.typography.bodySmall, color = color)
    }
}