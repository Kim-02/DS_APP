package com.example.ds_safer.ui.screens.main

import AuthViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.ui.screens.discovery.DiscoveryViewModel // ★ 패키지 경로 확인 필요!

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: DiscoveryViewModel,
    authViewModel: AuthViewModel,
    onNavigateToDetail: (JetsonDevice) -> Unit,
    onLogoutClick: () -> Unit
) {
    val registeredList by viewModel.registeredJetsons.collectAsState()
    val discoveredList by viewModel.discoveredJetsons.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("기기 연결") },
                actions = {
                TextButton(onClick = onLogoutClick) {
                    Text("로그아웃", color = Color.Red)
                }
            })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ==========================================
            // Section 1: 등록된 기기 (내 기기)
            // ==========================================
            item {
                Text(
                    text = "등록된 기기",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            if (registeredList.isEmpty()) {
                item {
                    Text(
                        "등록된 기기가 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            } else {
                items(registeredList) { device ->
                    JetsonDeviceItem(
                        device = device,
                        onClick = { onNavigateToDetail(device) } // ★ 이미 등록된 기기: 클릭 시 상세(리모컨) 화면으로 이동
                    )
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

            // ==========================================
            // Section 2: 연결 가능한 기기 (새로 찾은 기기)
            // ==========================================
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "연결 가능한 기기",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    if (isScanning) {
                        Spacer(Modifier.width(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    }
                }
            }

            if (discoveredList.isEmpty()) {
                item {
                    Text("주변 기기를 찾는 중...", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                items(discoveredList) { device ->
                    JetsonDeviceItem(
                        device = device,
                        onClick = { viewModel.registerDevice(device) } // ★ 새 기기: 타일 원클릭 시 통신 + 웹소켓 + 상단 이동 한방에 처리!
                    )
                }
            }
        }
    }
}

// ★ 수정됨: 불필요한 "등록" 버튼 파라미터를 싹 지우고, 진짜 블루투스 UI처럼 통일
@Composable
fun JetsonDeviceItem(
    device: JetsonDevice,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }, // 타일 전체 터치 영역화
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "IP: ${device.ipAddress} | Port: ${device.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            // 기기 항목 우측엔 항상 꺾쇠 화살표만 깔끔하게 표시
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "이동/연결",
                tint = Color.Gray
            )
        }
    }
}