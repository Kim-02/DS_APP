package com.example.ds_safer.ui.screens.main

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainDashboardScreen(
    viewModel: MainDashboardViewModel = viewModel(),
    onNavigateToJetson: () -> Unit,
    onNavigateToSensor: () -> Unit,
    onNavigateToCctv: () -> Unit
) {
    // 저장소의 젯슨 상태 구독
    val selectedJetson by viewModel.selectedJetson.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("DS-Safer 관리자 대시보드", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Jetson 관리 카드 (상태 반영)
        DashboardCard(
            title = "Jetson 등록",
            description = if (selectedJetson != null) {
                "연결됨: ${selectedJetson?.name}\n(${selectedJetson?.ipAddress})"
            } else {
                "연결된 기기 없음 (클릭하여 탐색)"
            },
            onClick = onNavigateToJetson,
            isRegistered = selectedJetson != null
        )

        // 2. 센서 관리 카드
        DashboardCard(title = "센서 등록", description = "대기 중인 센서 목록 확인", onClick = onNavigateToSensor)

        // 3. CCTV 관리 카드
        DashboardCard(title = "CCTV 등록", description = "카메라 정보 입력 및 등록", onClick = onNavigateToCctv)
    }
}

@Composable
fun DashboardCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    isRegistered: Boolean = false // [추가됨] 기본값은 false
) {
    // 등록 여부에 따라 카드의 배경색을 다르게 설정 (시각적 피드백)
    val cardColor = if (isRegistered) {
        MaterialTheme.colorScheme.primaryContainer // 등록되면 강조된 색상
    } else {
        MaterialTheme.colorScheme.surfaceVariant // 등록 전엔 차분한 색상
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor), // 배경색 적용
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isRegistered) Color.Unspecified else Color.Gray
                )
            }

            // 등록된 상태라면 체크 아이콘 표시 (선택 사항)
            if (isRegistered) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Registered",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}