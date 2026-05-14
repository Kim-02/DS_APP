package com.example.ds_safer.ui.screens.floormap

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorMapScreen(
    viewModel: FloorMapViewModel,
    onBackClick: () -> Unit
) {
    val floorMap by viewModel.floorMap.collectAsState()
    val availableSensors by viewModel.availableSensors.collectAsState()
    val placedSensors by viewModel.placedSensors.collectAsState()
    val selectedSensor by viewModel.selectedSensor.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    val bitmap: Bitmap? = remember(floorMap?.imageBase64) {
        floorMap?.imageBase64?.let { decodeBase64ToBitmap(it) }
    }

    // 온습도 센서만 배치 대상으로 표시
    val tempHumiditySensors = remember(availableSensors) {
        availableSensors.filter { it.sensorType == "temp_humidity" }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAll()
    }

    val selectedJetson = com.example.ds_safer.data.repository.JetsonRepository.selectedJetson.collectAsState()
    val spaceName = selectedJetson.value?.spaceName

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Column {
                        Text(floorMap?.mapName ?: "평면도 배치")
                        if (spaceName != null) {
                            Text(
                                text = spaceName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (message != null) {
                Text(
                    text = message ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = selectedSensor?.let {
                    "선택된 온습도 센서: ${it.senName}"
                } ?: "하단에서 온습도 센서를 선택한 뒤, 평면도를 탭하세요.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    .background(Color(0xFFF7F7F7), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }

                    bitmap == null -> {
                        Text("평면도 이미지를 불러올 수 없습니다.")
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .onSizeChanged { imageSize = it }
                                .pointerInput(selectedSensor, imageSize) {
                                    detectTapGestures { offset ->
                                        if (selectedSensor != null && imageSize.width > 0 && imageSize.height > 0) {
                                            val xRatio = (offset.x / imageSize.width).coerceIn(0f, 1f)
                                            val yRatio = (offset.y / imageSize.height).coerceIn(0f, 1f)
                                            viewModel.saveSensorPosition(xRatio, yRatio)
                                        }
                                    }
                                }
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "평면도",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                placedSensors
                                    .filter { it.sensorType == "temp_humidity" }
                                    .forEach { sensor ->
                                        val x = sensor.xRatio * size.width
                                        val y = sensor.yRatio * size.height

                                        drawCircle(
                                            color = Color(0xFF1976D2),
                                            radius = 14f,
                                            center = Offset(x, y)
                                        )
                                    }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "배치 가능한 온습도 센서",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (tempHumiditySensors.isEmpty()) {
                Text(
                    text = "배치 가능한 온습도 센서가 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tempHumiditySensors) { sensor ->
                        val isSelected = selectedSensor?.sensorId == sensor.sensorId

                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .clickable { viewModel.selectSensor(sensor) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = sensor.senName ?: "-",
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = sensor.sensorType ?: "-",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = sensor.senLocate ?: "-",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if ((sensor.isOnline ?: 0) == 1) "온라인" else "오프라인",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if ((sensor.isOnline ?: 0) == 1) {
                                        Color(0xFF2E7D32)
                                    } else {
                                        Color.Gray
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "현재는 온습도 센서만 배치할 수 있습니다. 센서를 선택한 뒤 평면도를 탭하면 위치가 저장됩니다.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}