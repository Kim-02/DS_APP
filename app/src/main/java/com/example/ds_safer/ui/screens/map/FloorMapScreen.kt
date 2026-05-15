package com.example.ds_safer.ui.screens.floormap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.AlertRepository
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.RecentAlertDto
import com.example.ds_safer.domain.model.RegisteredSensor
import com.example.ds_safer.domain.model.SensorMapPosition
import com.example.ds_safer.ui.theme.OnSafeCard
import com.example.ds_safer.ui.theme.OnSafeColor
import com.example.ds_safer.ui.theme.OnSafeScreenBrush
import com.example.ds_safer.ui.theme.OnSafeSectionTitle
import com.example.ds_safer.ui.theme.OnSafeSmallPill
import com.example.ds_safer.ui.theme.OnSafeStatusDot
import kotlin.math.roundToInt

@Composable
fun FloorMapScreen(
    viewModel: FloorMapViewModel,
    onBackClick: () -> Unit
) {
    val floorMap by viewModel.floorMap.collectAsState()
    val availableSensors by viewModel.availableSensors.collectAsState()
    val placedSensors by viewModel.placedSensors.collectAsState()
    val recentAlerts by viewModel.recentAlerts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var selectedSensorForPlace by remember { mutableStateOf<RegisteredSensor?>(null) }
    var selectedPlacedSensor by remember { mutableStateOf<SensorMapPosition?>(null) }
    var showPlaceGuide by remember { mutableStateOf(false) }
    var selectedAlert by remember { mutableStateOf<RecentAlertDto?>(null) }
    val scope = rememberCoroutineScope()

    // 알림 읽음 처리
    fun markAlertAsRead(alert: RecentAlertDto) {
        val eventId = alert.eventId ?: return
        if (alert.isRead == true) return
        AlertRepository.markAsRead(eventId)
        scope.launch {
            try {
                val device = JetsonRepository.selectedJetson.value ?: return@launch
                val service = RetrofitClient.createService("http://${device.ipAddress}:${device.port}/")
                service.markAlertAsRead(eventId, device.spaceId)
            } catch (_: Exception) {}
        }
    }

    val bitmap = remember(floorMap?.imageBase64) {
        floorMap?.imageBase64?.let { decodeBase64ToBitmap(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAll()
    }

    if (selectedPlacedSensor != null) {
        val sensor = selectedPlacedSensor!!
        AlertDialog(
            onDismissRequest = { selectedPlacedSensor = null },
            containerColor = OnSafeColor.Card,
            titleContentColor = OnSafeColor.TextPrimary,
            textContentColor = OnSafeColor.TextSecondary,
            title = {
                Text(sensor.senName ?: "센서 정보")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("센서 ID: ${sensor.sensorId}")
                    Text("상태: ${if (sensor.isOnline == 1) "온라인" else "오프라인"}")
                    Text("온도: ${sensor.latestTemp?.let { "%.1f°C".format(it) } ?: "-"}")
                    Text("습도: ${sensor.latestHumidity?.let { "%.1f%%".format(it) } ?: "-"}")
                    if (sensor.latestMeasuredAt != null) {
                        Text(
                            "측정 시각: ${sensor.latestMeasuredAt}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = OnSafeColor.TextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPlacedSensor = null }) {
                    Text("닫기", color = OnSafeColor.Blue)
                }
            }
        )
    }

    if (showPlaceGuide) {
        AlertDialog(
            onDismissRequest = { showPlaceGuide = false },
            containerColor = OnSafeColor.Card,
            titleContentColor = OnSafeColor.TextPrimary,
            textContentColor = OnSafeColor.TextSecondary,
            title = {
                Text("센서 배치 방법")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. 아래 배치 가능한 센서 목록에서 센서를 선택합니다.")
                    Text("2. 평면도에서 원하는 위치를 터치합니다.")
                    Text("3. 센서 위치가 map_id 기준으로 저장됩니다.")
                    Text("4. 서버에서 sensor.space_id와 floor_map.space_id 일치 여부를 검증합니다.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaceGuide = false }) {
                    Text("확인", color = OnSafeColor.Blue)
                }
            }
        )
    }

    Scaffold(
        containerColor = OnSafeColor.BgTop
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
                            text = "현장 평면도",
                            color = OnSafeColor.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = floorMap?.spaceName ?: floorMap?.mapName ?: "공간 정보를 불러오는 중...",
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { showPlaceGuide = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "안내",
                            tint = OnSafeColor.Blue
                        )
                    }

                    IconButton(onClick = { viewModel.loadAll() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "새로고침",
                            tint = OnSafeColor.Blue
                        )
                    }
                }
            }

            if (message != null) {
                item {
                    MessageBox(
                        message = message ?: "",
                        success = message?.contains("완료") == true,
                        onClick = { viewModel.clearMessage() }
                    )
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
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = OnSafeColor.Blue
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = OnSafeColor.Blue,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = floorMap?.spaceName ?: floorMap?.mapName ?: "평면도 미등록",
                                color = OnSafeColor.TextPrimary,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = floorMap?.let {
                                    "Map ID ${it.mapId} · 배치 센서 ${placedSensors.size}개 · 배치 가능 ${availableSensors.size}개"
                                } ?: "현재 공간에 등록된 평면도가 없습니다.",
                                color = OnSafeColor.TextSecondary,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )
                        }

                        OnSafeStatusDot(
                            color = if (floorMap != null) OnSafeColor.Green else OnSafeColor.Orange,
                            text = if (floorMap != null) "정상" else "필요"
                        )
                    }
                }
            }

            item {
                OnSafeSectionTitle("평면도")
            }

            item {
                OnSafeCard(modifier = Modifier.fillMaxWidth()) {
                    FloorMapCanvas(
                        bitmap = bitmap,
                        placedSensors = placedSensors,
                        selectedSensor = selectedSensorForPlace,
                        imageSize = imageSize,
                        onImageSizeChanged = { imageSize = it },
                        onPlacedSensorClick = { selectedPlacedSensor = it },
                        onMapTap = { xRatio, yRatio ->
                            val sensor = selectedSensorForPlace
                            val mapId = floorMap?.mapId
                            val sensorId = sensor?.sensorId

                            if (!sensorId.isNullOrBlank() && mapId != null) {
                                viewModel.saveSensorPosition(
                                    mapId = mapId,
                                    sensorId = sensorId,
                                    xRatio = xRatio,
                                    yRatio = yRatio
                                )
                                selectedSensorForPlace = null
                            }
                        }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LegendDot("정상", OnSafeColor.Green)
                    LegendDot("경고", OnSafeColor.Orange)
                    LegendDot("위험", OnSafeColor.Red)
                    LegendDot("오프라인", OnSafeColor.Gray)
                }
            }

            if (selectedSensorForPlace != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = OnSafeColor.Blue.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, OnSafeColor.Blue.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = OnSafeColor.Blue,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "${selectedSensorForPlace?.senName ?: "센서"} 선택됨. 평면도를 터치해 위치를 저장하세요.",
                                color = OnSafeColor.Blue,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                OnSafeSectionTitle("배치 가능한 센서")
            }

            if (availableSensors.isEmpty()) {
                item {
                    EmptyGuideCard(
                        title = "배치 가능한 센서가 없습니다.",
                        message = "현재 공간에 등록된 온습도 센서가 없거나 이미 모두 배치되었습니다."
                    )
                }
            } else {
                items(
                    items = availableSensors,
                    key = { it.sensorId ?: it.senId.toString() }
                ) { sensor ->
                    AvailableSensorCard(
                        sensor = sensor,
                        selected = selectedSensorForPlace?.sensorId == sensor.sensorId,
                        onClick = {
                            selectedSensorForPlace =
                                if (selectedSensorForPlace?.sensorId == sensor.sensorId) {
                                    null
                                } else {
                                    sensor
                                }
                        }
                    )
                }
            }

            item {
                OnSafeSectionTitle("최근 알림")
            }

            // 알림 상세 모달
            selectedAlert?.let { alert ->
                item {
                    AlertDialog(
                        onDismissRequest = { selectedAlert = null },
                        containerColor = OnSafeColor.Card,
                        titleContentColor = OnSafeColor.TextPrimary,
                        textContentColor = OnSafeColor.TextSecondary,
                        title = { Text(alert.title ?: "알림 상세") },
                        text = {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState()).padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (!alert.createdAt.isNullOrBlank()) Text("발생 시간: ${alert.createdAt}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = OnSafeColor.TextSecondary)
                                if (!alert.cameraName.isNullOrBlank()) Text("카메라: ${alert.cameraName}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = OnSafeColor.TextSecondary)
                                if (!alert.level.isNullOrBlank()) Text("등급: ${alert.level}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = OnSafeColor.TextSecondary)
                                Spacer(Modifier.height(4.dp))
                                Text(alert.message ?: "-", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = OnSafeColor.TextPrimary)
                            }
                        },
                        confirmButton = { TextButton(onClick = { selectedAlert = null }) { Text("닫기", color = OnSafeColor.Blue) } },
                    )
                }
            }

            item {
                if (recentAlerts.isEmpty()) {
                    OnSafeCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "최근 알림이 없습니다.",
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                } else {
                    OnSafeCard(modifier = Modifier.fillMaxWidth()) {
                        recentAlerts.take(3).forEachIndexed { index, alert ->
                            val levelColor = when (alert.level) {
                                "danger"  -> OnSafeColor.Red
                                "warning" -> Color(0xFFFF9800)
                                else      -> OnSafeColor.Blue
                            }
                            AlertRow(
                                color = levelColor,
                                title = alert.title ?: "알림",
                                desc = alert.message ?: "",
                                time = alert.createdAt?.takeLast(8)?.take(5) ?: "",
                                badge = when (alert.level) { "danger" -> "위험"; "warning" -> "주의"; else -> "정보" },
                                onClick = {
                                    selectedAlert = alert
                                    markAlertAsRead(alert)
                                },
                            )
                            if (index < recentAlerts.take(3).size - 1) {
                                Divider(
                                    color = OnSafeColor.StrokeSoft,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun FloorMapCanvas(
    bitmap: Bitmap?,
    placedSensors: List<SensorMapPosition>,
    selectedSensor: RegisteredSensor?,
    imageSize: IntSize,
    onImageSizeChanged: (IntSize) -> Unit,
    onPlacedSensorClick: (SensorMapPosition) -> Unit,
    onMapTap: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .background(OnSafeColor.CardDark, RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = OnSafeColor.Stroke,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = OnSafeColor.Orange,
                    modifier = Modifier.size(34.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "등록된 평면도가 없습니다.",
                    color = OnSafeColor.TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "현재 space_id에 연결된 floor_map을 확인해주세요.",
                    color = OnSafeColor.TextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { onImageSizeChanged(it) }
                    .pointerInput(selectedSensor) {
                        detectTapGestures { offset ->
                            if (selectedSensor != null && imageSize.width > 0 && imageSize.height > 0) {
                                val xRatio = (offset.x / imageSize.width).coerceIn(0f, 1f)
                                val yRatio = (offset.y / imageSize.height).coerceIn(0f, 1f)
                                onMapTap(xRatio, yRatio)
                            }
                        }
                    }
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "현장 평면도",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                placedSensors.forEach { sensor ->
                    val x = (sensor.xRatio * imageSize.width).roundToInt()
                    val y = (sensor.yRatio * imageSize.height).roundToInt()

                    SensorMapMarker(
                        sensor = sensor,
                        modifier = Modifier.offset {
                            IntOffset(
                                x = x - 14,
                                y = y - 14
                            )
                        },
                        onClick = {
                            onPlacedSensorClick(sensor)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SensorMapMarker(
    sensor: SensorMapPosition,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val sensorType = sensor.sensorType ?: ""

    val color = when {
        sensor.isOnline == 0 -> OnSafeColor.Gray
        sensorType.contains("gas", ignoreCase = true) -> OnSafeColor.Orange
        sensorType.contains("fire", ignoreCase = true) -> OnSafeColor.Red
        else -> OnSafeColor.Green
    }

    Box(
        modifier = modifier
            .size(28.dp)
            .background(color.copy(alpha = 0.20f), CircleShape)
            .border(1.dp, color, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = sensorMarkerText(sensorType),
            color = color,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun AvailableSensorCard(
    sensor: RegisteredSensor,
    selected: Boolean,
    onClick: () -> Unit
) {
    val sensorType = sensor.sensorType ?: ""
    val sensorId = sensor.sensorId ?: ""

    OnSafeCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        selected = selected
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(OnSafeColor.Green.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sensorMarkerText(sensorType),
                    color = OnSafeColor.Green,
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sensor.senName ?: "-",
                    color = OnSafeColor.TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "ID: $sensorId",
                    color = OnSafeColor.TextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "위치: ${sensor.senLocate ?: "미지정"}",
                    color = OnSafeColor.TextTertiary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = OnSafeColor.Blue
                )
            } else {
                OnSafeSmallPill("배치")
            }
        }
    }
}

@Composable
private fun LegendDot(
    text: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = text,
            color = OnSafeColor.TextSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun AlertRow(
    color: Color,
    title: String,
    desc: String,
    time: String,
    badge: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(color.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = OnSafeColor.TextPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )

            Text(
                text = desc,
                color = OnSafeColor.TextSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = time,
                color = OnSafeColor.TextSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            OnSafeSmallPill(
                text = badge,
                color = if (badge == "경고") OnSafeColor.Orange else OnSafeColor.Blue
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
private fun MessageBox(
    message: String,
    success: Boolean,
    onClick: () -> Unit
) {
    val color = if (success) OnSafeColor.Green else OnSafeColor.Orange
    val icon = if (success) Icons.Default.Check else Icons.Default.Warning

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

private fun sensorMarkerText(sensorType: String): String {
    val lower = sensorType.lowercase()
    return when {
        lower.contains("temp") || lower.contains("humid") || lower.contains("온습") -> "T"
        lower.contains("gas") || lower.contains("co") -> "G"
        lower.contains("fire") || lower.contains("smoke") -> "F"
        lower.contains("camera") || lower.contains("cctv") -> "C"
        else -> "S"
    }
}