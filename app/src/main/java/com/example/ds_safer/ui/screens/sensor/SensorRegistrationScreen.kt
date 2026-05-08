package com.example.ds_safer.ui.screens.sensor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ds_safer.domain.model.DiscoveredSensor
import com.example.ds_safer.domain.model.WorkerDbResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorRegistrationScreen(
    viewModel: SensorRegistrationViewModel,
    onBackClick: () -> Unit
) {
    val readySensors by viewModel.readySensors.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val selectedSensor by viewModel.selectedSensor.collectAsState()
    val selectedWorker by viewModel.selectedWorker.collectAsState()
    val registerSuccess by viewModel.registerSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var selectedNormalSensors by remember { mutableStateOf(setOf<DiscoveredSensor>()) }

    val heartBandSensors = readySensors.filter { it.sensorType == "heart_band" }
    val normalSensors = readySensors.filter { it.sensorType != "heart_band" }

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            viewModel.consumeRegisterSuccess()
            // 바로 뒤로가기 싫으면 아래 줄을 주석 처리하세요.
            // onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새 센서 등록") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("뒤로")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.assignSelectedWatchToWorker()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLoading && selectedSensor != null && selectedWorker != null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("선택한 워치를 작업자에게 등록")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.registerMultipleSensors(selectedNormalSensors.toList())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLoading && selectedNormalSensors.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("일반 센서 ${selectedNormalSensors.size}개 등록")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (message != null) {
                AssistChip(
                    onClick = { viewModel.consumeMessage() },
                    label = { Text(message ?: "") }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        "1. 발견된 워치 선택",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (heartBandSensors.isEmpty()) {
                        Text(
                            "발견된 heart_band 워치가 없습니다.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                items(heartBandSensors) { sensor ->
                    HeartBandSensorItem(
                        sensor = sensor,
                        selected = selectedSensor?.sensorId == sensor.sensorId,
                        onClick = { viewModel.selectSensor(sensor) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "2. 작업자 선택",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (workers.isEmpty()) {
                        Text(
                            "조회된 작업자가 없습니다.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                items(workers) { worker ->
                    WorkerSelectItem(
                        worker = worker,
                        selected = selectedWorker?.deptId == worker.deptId,
                        onClick = { viewModel.selectWorker(worker) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "3. 일반 센서 등록",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (normalSensors.isEmpty()) {
                        Text(
                            "등록 가능한 일반 센서가 없습니다.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                items(normalSensors) { sensor ->
                    val isChecked = selectedNormalSensors.contains(sensor)

                    NormalSensorItem(
                        sensor = sensor,
                        checked = isChecked,
                        onClick = {
                            selectedNormalSensors = if (isChecked) {
                                selectedNormalSensors - sensor
                            } else {
                                selectedNormalSensors + sensor
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeartBandSensorItem(
    sensor: DiscoveredSensor,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = if (selected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(sensor.senName, style = MaterialTheme.typography.titleMedium)
                Text("ID: ${sensor.sensorId}", style = MaterialTheme.typography.bodySmall)
                Text("타입: ${sensor.sensorType}", style = MaterialTheme.typography.bodySmall)
                Text("위치: ${sensor.senLocate}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "상태: ${if (sensor.isOnline == true) "온라인" else "알 수 없음"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun WorkerSelectItem(
    worker: WorkerDbResponse,
    selected: Boolean,
    onClick: () -> Unit
) {
    val alreadyAssigned = worker.senId != null
    val enabled = worker.isManager == 0 && !alreadyAssigned

    val assignedText = if (alreadyAssigned) {
        "배정됨: ${worker.sensorName ?: worker.sensorId ?: worker.senId}"
    } else {
        "미배정"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = enabled) { onClick() },
        colors = if (selected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else if (!enabled) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                enabled = enabled
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    "${worker.name} / 사번 ${worker.deptId}",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "구분: ${if (worker.isManager == 1) "관리자" else "작업자"}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    "센서: $assignedText",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun NormalSensorItem(
    sensor: DiscoveredSensor,
    checked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(sensor.senName, style = MaterialTheme.typography.titleMedium)
                Text("ID: ${sensor.sensorId}", style = MaterialTheme.typography.bodySmall)
                Text("타입: ${sensor.sensorType}", style = MaterialTheme.typography.bodySmall)
                Text("위치: ${sensor.senLocate}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}