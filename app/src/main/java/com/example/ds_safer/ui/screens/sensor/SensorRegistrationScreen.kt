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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorRegistrationScreen(
    viewModel: SensorRegistrationViewModel,
    onBackClick: () -> Unit
) {
    val readySensors by viewModel.readySensors.collectAsState()
    val registerSuccess by viewModel.registerSuccess.collectAsState()

    var selectedSensors by remember { mutableStateOf(setOf<DiscoveredSensor>()) }

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            viewModel.consumeRegisterSuccess()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("새 센서 등록 (다중 선택)") })
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.registerMultipleSensors(selectedSensors.toList())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(16.dp)
                    .height(56.dp),
                enabled = selectedSensors.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("선택한 ${selectedSensors.size}개 센서 등록하기", style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("젯슨 주변에서 발견된 센서", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            if (readySensors.isEmpty()) {
                Text("발견된 새 센서가 없습니다.", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn {
                    items(readySensors) { sensor ->
                        val isChecked = selectedSensors.contains(sensor)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedSensors = if (isChecked) {
                                        selectedSensors - sensor
                                    } else {
                                        selectedSensors + sensor
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(sensor.senName, style = MaterialTheme.typography.titleMedium)
                                    Text("타입: ${sensor.sensorType}", style = MaterialTheme.typography.bodySmall)
                                    Text("위치: ${sensor.senLocate}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
