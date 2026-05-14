package com.example.ds_safer.ui.screens.sensor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ds_safer.domain.model.DiscoveredSensor
import com.example.ds_safer.domain.model.WorkerDbResponse
import com.example.ds_safer.ui.theme.OnSafeCard
import com.example.ds_safer.ui.theme.OnSafeColor
import com.example.ds_safer.ui.theme.OnSafePrimaryButton
import com.example.ds_safer.ui.theme.OnSafeScreenBrush
import com.example.ds_safer.ui.theme.OnSafeSectionTitle
import com.example.ds_safer.ui.theme.OnSafeSmallPill
import com.example.ds_safer.ui.theme.OnSafeStatusDot

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
    var showHelpDialog by remember { mutableStateOf(false) }

    val heartBandSensors = readySensors.filter { it.sensorType == "heart_band" }
    val normalSensors = readySensors.filter { it.sensorType != "heart_band" }

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            selectedNormalSensors = emptySet()
            viewModel.consumeRegisterSuccess()
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = OnSafeColor.Card,
            titleContentColor = OnSafeColor.TextPrimary,
            textContentColor = OnSafeColor.TextSecondary,
            title = { Text("센서 등록 안내") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• 워치형 센서는 작업자와 1:1로 매칭해서 등록합니다.")
                    Text("• 일반 센서는 여러 개를 선택해서 한 번에 등록할 수 있습니다.")
                    Text("• 센서의 공간 정보는 현재 선택된 Jetson의 space_id를 기준으로 서버에서 저장됩니다.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("확인", color = OnSafeColor.Blue)
                }
            }
        )
    }

    Scaffold(
        containerColor = OnSafeColor.BgTop,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OnSafeColor.CardDark)
                    .systemBarsPadding()
                    .padding(16.dp)
            ) {
                OnSafePrimaryButton(
                    text = if (isLoading) "워치 등록 중..." else "선택한 워치를 작업자에게 등록",
                    enabled = !isLoading && selectedSensor != null && selectedWorker != null,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.assignSelectedWatchToWorker()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OnSafePrimaryButton(
                    text = if (isLoading) {
                        "센서 등록 중..."
                    } else {
                        "일반 센서 ${selectedNormalSensors.size}개 등록"
                    },
                    enabled = !isLoading && selectedNormalSensors.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.registerMultipleSensors(selectedNormalSensors.toList())
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(OnSafeScreenBrush)
                .padding(padding)
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = OnSafeColor.TextPrimary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "센서 등록",
                            color = OnSafeColor.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "워치와 일반 센서를 현재 현장에 등록합니다.",
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "안내",
                            tint = OnSafeColor.Blue
                        )
                    }
                }
            }

            item {
                OnSafeCard(
                    modifier = Modifier.fillMaxWidth(),
                    selected = true
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(OnSafeColor.Blue.copy(alpha = 0.18f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = OnSafeColor.Blue,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "센서 검색 결과",
                                color = OnSafeColor.TextPrimary,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "워치 ${heartBandSensors.size}개 · 일반 센서 ${normalSensors.size}개 · 작업자 ${workers.size}명",
                                color = OnSafeColor.TextSecondary,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = OnSafeColor.Blue
                            )
                        } else {
                            OnSafeSmallPill("대기")
                        }
                    }
                }
            }

            message?.let { msg ->
                item {
                    MessageBox(
                        message = msg,
                        color = if (registerSuccess) OnSafeColor.Green else OnSafeColor.Blue,
                        icon = if (registerSuccess) Icons.Default.Check else Icons.Default.Info,
                        onClick = { viewModel.consumeMessage() }
                    )
                }
            }

            item {
                OnSafeSectionTitle("1. 발견된 워치 선택")
            }

            if (heartBandSensors.isEmpty()) {
                item {
                    EmptyGuideCard(
                        title = "발견된 heart_band 워치가 없습니다.",
                        message = "워치 전원, MQTT 연결, mDNS 광고 상태를 확인해주세요."
                    )
                }
            } else {
                items(heartBandSensors) { sensor ->
                    HeartBandSensorItem(
                        sensor = sensor,
                        selected = selectedSensor?.sensorId == sensor.sensorId,
                        onClick = { viewModel.selectSensor(sensor) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                OnSafeSectionTitle("2. 작업자 선택")
            }

            if (workers.isEmpty()) {
                item {
                    EmptyGuideCard(
                        title = "조회된 작업자가 없습니다.",
                        message = "서버의 작업자 DB 또는 네트워크 연결 상태를 확인해주세요."
                    )
                }
            } else {
                items(workers) { worker ->
                    WorkerSelectItem(
                        worker = worker,
                        selected = selectedWorker?.deptId == worker.deptId,
                        onClick = { viewModel.selectWorker(worker) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                OnSafeSectionTitle("3. 일반 센서 등록")
            }

            if (normalSensors.isEmpty()) {
                item {
                    EmptyGuideCard(
                        title = "등록 가능한 일반 센서가 없습니다.",
                        message = "온습도/가스/화재 센서가 온라인 상태인지 확인해주세요."
                    )
                }
            } else {
                items(normalSensors) { sensor ->
                    val checked = selectedNormalSensors.contains(sensor)

                    NormalSensorItem(
                        sensor = sensor,
                        checked = checked,
                        onClick = {
                            selectedNormalSensors = if (checked) {
                                selectedNormalSensors - sensor
                            } else {
                                selectedNormalSensors + sensor
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))
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
    OnSafeCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        selected = selected
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = OnSafeColor.Blue,
                    unselectedColor = OnSafeColor.TextSecondary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            SensorIconCircle(
                text = "♥",
                color = OnSafeColor.Orange
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sensor.senName,
                        color = OnSafeColor.TextPrimary,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    OnSafeStatusDot(
                        color = if (sensor.isOnline == true) OnSafeColor.Green else OnSafeColor.Gray,
                        text = if (sensor.isOnline == true) "온라인" else "알 수 없음"
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ID: ${sensor.sensorId}",
                    color = OnSafeColor.TextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "위치: ${sensor.senLocate}",
                    color = OnSafeColor.TextTertiary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
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

    OnSafeCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        selected = selected
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                colors = RadioButtonDefaults.colors(
                    selectedColor = OnSafeColor.Blue,
                    unselectedColor = OnSafeColor.TextSecondary,
                    disabledSelectedColor = OnSafeColor.Gray,
                    disabledUnselectedColor = OnSafeColor.Gray
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            SensorIconCircle(
                text = "작",
                color = if (enabled) OnSafeColor.Blue else OnSafeColor.Gray
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${worker.name} / 사번 ${worker.deptId}",
                        color = if (enabled) OnSafeColor.TextPrimary else OnSafeColor.TextSecondary,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    OnSafeSmallPill(
                        text = if (worker.isManager == 1) "관리자" else "작업자",
                        color = if (enabled) OnSafeColor.Blue else OnSafeColor.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "센서: $assignedText",
                    color = if (alreadyAssigned) OnSafeColor.Orange else OnSafeColor.TextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
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
    OnSafeCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        selected = checked
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = OnSafeColor.Blue,
                    uncheckedColor = OnSafeColor.TextSecondary,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            SensorIconCircle(
                text = sensorIconText(sensor.sensorType),
                color = sensorIconColor(sensor.sensorType)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sensor.senName,
                    color = OnSafeColor.TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ID: ${sensor.sensorId}",
                    color = OnSafeColor.TextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "타입: ${sensor.sensorType} · 위치: ${sensor.senLocate}",
                    color = OnSafeColor.TextTertiary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = OnSafeColor.Blue
                )
            }
        }
    }
}

@Composable
private fun SensorIconCircle(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(color.copy(alpha = 0.16f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun EmptyGuideCard(
    title: String,
    message: String
) {
    OnSafeCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = OnSafeColor.Orange,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = OnSafeColor.TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message,
                    color = OnSafeColor.TextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun MessageBox(
    message: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = message,
                color = color,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun sensorIconText(sensorType: String): String {
    val lower = sensorType.lowercase()
    return when {
        lower.contains("heart") || lower.contains("wear") || lower.contains("심박") -> "♥"
        lower.contains("temp") || lower.contains("humid") || lower.contains("온습") -> "℃"
        lower.contains("gas") || lower.contains("co") -> "G"
        lower.contains("fire") || lower.contains("smoke") -> "!"
        else -> "S"
    }
}

private fun sensorIconColor(sensorType: String): Color {
    val lower = sensorType.lowercase()
    return when {
        lower.contains("heart") || lower.contains("wear") || lower.contains("심박") -> OnSafeColor.Orange
        lower.contains("temp") || lower.contains("humid") || lower.contains("온습") -> OnSafeColor.Green
        lower.contains("gas") || lower.contains("co") -> OnSafeColor.Blue
        lower.contains("fire") || lower.contains("smoke") -> OnSafeColor.Red
        else -> OnSafeColor.Blue
    }
}