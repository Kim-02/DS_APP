package com.example.ds_safer.ui.screens.cctv

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CctvListScreen(
    viewModel: CctvListViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    onBackClick: () -> Unit // 필요시 상단 뒤로가기 버튼 등에 사용
) {
    val cameraList by viewModel.cameraList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 화면 켜질 때 자동으로 서버에서 목록 불러오기
    LaunchedEffect(Unit) {
        viewModel.fetchCameras()
    }

    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)
    ) {
        Text("등록된 CCTV 관리", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // 상단: CCTV 목록 리스트
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (cameraList.isEmpty()) {
                    item {
                        Text("등록된 CCTV가 없습니다.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(cameraList) { camera ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // ★ 수정됨: 파이썬 API 규격에 맞춘 데이터 표시
                                Text(
                                    text = camera.senName ?: "이름 없는 카메라",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "위치: ${camera.senLocate ?: "미지정"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "IP 주소: ${camera.ipAddress}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // ★ 상태(health)에 따른 동적 색상 처리 (정상이면 파란색, 아니면 빨간색)
                                val isHealthy = camera.health == "normal" || camera.health == "good"
                                Text(
                                    text = "상태: ${camera.health ?: "상태 확인 불가"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isHealthy) Color(0xFF1976D2) else Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 하단: 등록 화면으로 넘어가는 버튼
        Button(
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("새 CCTV 등록하기")
        }
    }
}