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
    viewModel: SensorRegistrationViewModel, // 뷰모델 연결
    onBackClick: () -> Unit
) {
    // 1. 서버에서 받아온 대기 중인 센서 목록
    val readySensors by viewModel.readySensors.collectAsState()

    // 2. 사용자가 체크박스에 체크한 센서들을 기억하는 주머니 (Set으로 중복 방지)
    var selectedSensors by remember { mutableStateOf(setOf<DiscoveredSensor>()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("새 센서 등록 (다중 선택)") })
        },
        // 화면 맨 아래에 항상 고정되는 [저장] 버튼
        bottomBar = {
            Button(
                onClick = {
                    viewModel.registerMultipleSensors(selectedSensors.toList())
                    onBackClick() // 저장 후 이전 화면으로 돌아가기
                },
                modifier = Modifier.fillMaxWidth().systemBarsPadding().padding(16.dp).height(56.dp),
                enabled = selectedSensors.isNotEmpty(), // 1개라도 체크해야 버튼 활성화!
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

                        // 센서 1줄 (체크박스 + 이름)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    // 클릭 시 체크 ↔ 해제 토글 로직
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
                                    onCheckedChange = null // 카드 전체 클릭으로 제어하므로 여기선 null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(sensor.senName, style = MaterialTheme.typography.titleMedium)
                                    Text("타입: ${sensor.sensorType}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}