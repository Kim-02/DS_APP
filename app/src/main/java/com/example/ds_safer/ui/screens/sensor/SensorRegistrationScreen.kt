package com.example.ds_safer.ui.screens.sensor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ds_safer.domain.model.SensorInfo

@Composable
fun SensorRegistrationScreen(
    viewModel: SensorRegistrationViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("등록 가능한 센서 목록", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is SensorUiState.Loading -> CircularProgressIndicator()
            is SensorUiState.Error -> Text(state.message, color = Color.Red)
            is SensorUiState.Success -> {
                if (state.sensors.isEmpty()) {
                    Text("현재 대기 중인 센서가 없습니다.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.sensors) { sensor ->
                            SensorItem(sensor) {
                                viewModel.registerSensor(sensor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorItem(sensor: SensorInfo, onRegisterClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "ID: ${sensor.id}", style = MaterialTheme.typography.titleMedium)
                Text(text = "Type: ${sensor.type}", style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onRegisterClick) {
                Text("등록")
            }
        }
    }
}