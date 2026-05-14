package com.example.ds_safer.ui.screens.sensor

import android.util.Log
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ds_safer.ui.theme.OnSafeCard
import com.example.ds_safer.ui.theme.OnSafeColor
import com.example.ds_safer.ui.theme.OnSafePrimaryButton
import com.example.ds_safer.ui.theme.OnSafeScreenBrush
import com.example.ds_safer.ui.theme.OnSafeSectionTitle
import com.example.ds_safer.ui.theme.OnSafeSmallPill
import com.example.ds_safer.ui.theme.OnSafeStatusDot

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
            containerColor = OnSafeColor.Card,
            titleContentColor = OnSafeColor.TextPrimary,
            textContentColor = OnSafeColor.TextSecondary,
            title = { Text("센서 등록 해제") },
            text = { Text("이 센서를 등록 해제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val sensorId = targetSensorId
                        targetSensorId = null
                        if (!sensorId.isNullOrBlank()) {
                            viewModel.unregisterSensor(sensorId)
                        }
                    }
                ) {
                    Text("해제", color = OnSafeColor.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { targetSensorId = null }) {
                    Text("취소", color = OnSafeColor.Blue)
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
                    text = "새 센서 등록하기",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && !isUnregistering,
                    onClick = onNavigateToRegister
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
                            text = "등록된 센서 관리",
                            color = OnSafeColor.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "현재 Jetson에 등록된 센서 목록입니다.",
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(onClick = { viewModel.fetchSensors() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "새로고침",
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
                                tint = OnSafeColor.Blue
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "센서 현황",
                                color = OnSafeColor.TextPrimary,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "전체 ${sensorList.size}개 · 온라인 ${sensorList.count { it.isOnline == 1 }}개",
                                color = OnSafeColor.TextSecondary,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )
                        }

                        if (isLoading || isUnregistering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = OnSafeColor.Blue
                            )
                        } else {
                            OnSafeSmallPill("관리")
                        }
                    }
                }
            }

            errorMessage?.let {
                item {
                    ErrorBox(it)
                }
            }

            item {
                OnSafeSectionTitle("등록된 센서")
            }

            if (isLoading) {
                item {
                    LoadingBox("센서 목록을 불러오는 중입니다.")
                }
            } else if (sensorList.isEmpty()) {
                item {
                    EmptyGuideCard(
                        title = "등록된 센서가 없습니다.",
                        message = "새 센서 등록하기 버튼을 눌러 센서를 등록해주세요."
                    )
                }
            } else {
                items(sensorList) { sensor ->
                    SensorManageCard(
                        name = sensor.senName ?: "-",
                        type = sensor.sensorType ?: "-",
                        location = sensor.senLocate ?: "-",
                        sensorId = sensor.sensorId ?: "-",
                        online = sensor.isOnline == 1,
                        onDeleteClick = {
                            if (!sensor.sensorId.isNullOrBlank()) {
                                targetSensorId = sensor.sensorId
                            }
                        },
                        deleteEnabled = !isUnregistering && !sensor.sensorId.isNullOrBlank()
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
private fun SensorManageCard(
    name: String,
    type: String,
    location: String,
    sensorId: String,
    online: Boolean,
    deleteEnabled: Boolean,
    onDeleteClick: () -> Unit
) {
    OnSafeCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(sensorIconColor(type).copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sensorIconText(type),
                    color = sensorIconColor(type),
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        color = OnSafeColor.TextPrimary,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    OnSafeStatusDot(
                        color = if (online) OnSafeColor.Green else OnSafeColor.Gray,
                        text = if (online) "온라인" else "오프라인"
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "타입: $type · 위치: $location",
                    color = OnSafeColor.TextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "ID: $sensorId",
                    color = OnSafeColor.TextTertiary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                modifier = Modifier
                    .clickable(enabled = deleteEnabled) {
                        onDeleteClick()
                    },
                shape = RoundedCornerShape(12.dp),
                color = if (deleteEnabled) {
                    OnSafeColor.Red.copy(alpha = 0.12f)
                } else {
                    OnSafeColor.Gray.copy(alpha = 0.10f)
                },
                border = BorderStroke(
                    1.dp,
                    if (deleteEnabled) OnSafeColor.Red.copy(alpha = 0.4f)
                    else OnSafeColor.Gray.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = if (deleteEnabled) OnSafeColor.Red else OnSafeColor.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "해제",
                        color = if (deleteEnabled) OnSafeColor.Red else OnSafeColor.Gray,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingBox(message: String) {
    OnSafeCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = OnSafeColor.Blue
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                color = OnSafeColor.TextSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
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
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = OnSafeColor.Blue,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = OnSafeColor.TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )

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
private fun ErrorBox(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = OnSafeColor.Red.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, OnSafeColor.Red.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = OnSafeColor.Red,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = message,
                color = OnSafeColor.Red,
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