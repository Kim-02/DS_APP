package com.example.ds_safer.ui.screens.sensor

import android.util.Log
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
    val isUnregistering by viewModel.isUnregistering.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var targetSensorId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        Log.d("SensorListScreen", "Screen entered")
        viewModel.fetchSensors()
    }

    if (targetSensorId != null) {
        AlertDialog(
            onDismissRequest = { targetSensorId = null },
            title = { Text("센서 등록 해제") },
            text = { Text("이 센서를 등록 해제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val sensorId = targetSensorId
                        targetSensorId = null
                        if (sensorId != null) {
                            viewModel.unregisterSensor(sensorId)
                        }
                    }
                ) {
                    Text("해제")
                }
            },
            dismissButton = {
                TextButton(onClick = { targetSensorId = null }) {
                    Text("취소")
                }
            }
        )
    }

    errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            // 필요하면 Snackbar로 바꾸셔도 됩니다.
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        Text("등록된 센서 관리", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (sensorList.isEmpty()) {
                    item {
                        Text(
                            "등록된 센서가 없습니다.",
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(sensorList) { sensor ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "이름: ${sensor.senName ?: "-"}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "타입: ${sensor.sensorType ?: "-"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "위치: ${sensor.senLocate ?: "-"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                sensor.sensorId.let { id ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "ID: $id",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                sensor.isOnline?.let { online ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (online == 1) "상태: 온라인" else "상태: 오프라인",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (online == 1) Color(0xFF2E7D32) else Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        targetSensorId = sensor.sensorId
                                    },
                                    enabled = !isUnregistering,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("등록 해제")
                                }
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
            Text("새 센서 등록하기")
        }
    }
}
