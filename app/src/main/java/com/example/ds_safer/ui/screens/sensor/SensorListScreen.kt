package com.example.ds_safer.ui.screens.sensor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.systemBarsPadding

@Composable
fun SensorListScreen(
    viewModel: SensorListViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    onBackClick: () -> Unit
) {
    val sensorList by viewModel.sensorList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 화면 진입 시 데이터 자동 로드
    LaunchedEffect(Unit) {
        viewModel.fetchSensors()
    }

    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)) {
        Text("등록된 센서 관리", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (sensorList.isEmpty()) {
                    item {
                        Text("등록된 센서가 없습니다.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(sensorList) { sensor ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("이름: ${sensor.senName}", style = MaterialTheme.typography.titleMedium)
                                Text("타입: ${sensor.sensorType}", style = MaterialTheme.typography.bodyMedium)
                                Text("위치: ${sensor.senLocate}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("새 센서 등록하기")
        }
    }
}