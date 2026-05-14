package com.example.ds_safer.ui.screens.cctv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CctvListScreen(
    viewModel: CctvListViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    onBackClick: () -> Unit
) {
    val cameraList by viewModel.cameraList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCameras()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "등록된 CCTV 관리",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedButton(
                onClick = onBackClick
            ) {
                Text("뒤로")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (isLoading) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CCTV 목록을 불러오는 중입니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (cameraList.isEmpty()) {
                    item {
                        Text(
                            text = "등록된 CCTV가 없습니다.",
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(
                        items = cameraList,
                        key = { camera -> camera.id }
                    ) { camera ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = camera.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    TextButton(
                                        onClick = {
                                            viewModel.deleteCamera(camera.id)
                                        },
                                        enabled = !isLoading
                                    ) {
                                        Text("삭제", color = Color.Red)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Sensor ID: ${camera.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                Text(
                                    text = "Device ID: ${camera.deviceId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                Text(
                                    text = "Space ID: ${camera.spaceId ?: "미지정"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                camera.spaceName?.let {
                                    Text(
                                        text = "공간: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (camera.isActive) "상태: 활성화" else "상태: 비활성화",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (camera.isActive) Color(0xFF1976D2) else Color.Red
                                )

                                camera.camera?.rtspUrl?.let { rtspUrl ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "RTSP: $rtspUrl",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "등록일: ${camera.registeredAt}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("새 CCTV 등록하기")
        }
    }
}