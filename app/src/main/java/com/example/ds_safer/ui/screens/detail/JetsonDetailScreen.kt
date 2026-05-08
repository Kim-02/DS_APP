package com.example.ds_safer.ui.screens.detail

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.domain.model.FloorMapInfo
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.domain.model.LatestTempHumidityData
import com.example.ds_safer.domain.model.SensorMapPosition
import com.example.ds_safer.ui.screens.floormap.decodeBase64ToBitmap
import com.example.ds_safer.ui.screens.monitor.MonitoringActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JetsonDetailScreen(
    device: JetsonDevice,
    onDisconnectClick: () -> Unit,
    onNavigateToSensorRegister: () -> Unit,
    onNavigateToCctvRegister: () -> Unit,
    onNavigateToFloorMap: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToFloorMap) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "평면도 배치"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("IP 주소: ${device.ipAddress}", style = MaterialTheme.typography.bodyLarge)
                    Text("포트: ${device.port}", style = MaterialTheme.typography.bodyLarge)
                    Text("기기 ID: ${device.jetsonId}", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            MenuButton(
                icon = Icons.Default.Build,
                text = "센서 등록하기 (다중 선택)",
                onClick = onNavigateToSensorRegister
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuButton(
                icon = Icons.Default.Add,
                text = "CCTV 등록하기",
                onClick = onNavigateToCctvRegister
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDisconnectClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "이 젯슨 기기 연결 해제",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "온습도 센서 위치",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            JetsonFloorMapPreview(
                device = device,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun JetsonFloorMapPreview(
    device: JetsonDevice,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var floorMap by remember { mutableStateOf<FloorMapInfo?>(null) }
    var placedSensors by remember { mutableStateOf<List<SensorMapPosition>>(emptyList()) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var popupSensor by remember { mutableStateOf<SensorMapPosition?>(null) }
    var popupLatest by remember { mutableStateOf<LatestTempHumidityData?>(null) }
    var popupLoading by remember { mutableStateOf(false) }

    LaunchedEffect(device.ipAddress, device.port, device.jetsonId) {
        val jetsonId = device.jetsonId
        if (jetsonId == null) {
            isLoading = false
            errorMessage = "젯슨 ID가 없습니다."
            return@LaunchedEffect
        }

        try {
            isLoading = true
            errorMessage = null

            val baseUrl = "http://${device.ipAddress}:${device.port}/"
            val service = RetrofitClient.createService(baseUrl)

            val mapResponse = service.getFloorMap(jetsonId)
            if (mapResponse.status == "success") {
                floorMap = mapResponse.data
                bitmap = decodeBase64ToBitmap(mapResponse.data.imageBase64)

                val sensorResponse = service.getMapSensorPositions(mapResponse.data.mapId)
                if (sensorResponse.status == "success") {
                    placedSensors = sensorResponse.data.filter { it.sensorType == "temp_humidity" }
                } else {
                    placedSensors = emptyList()
                }
            } else {
                errorMessage = "평면도 정보를 불러오지 못했습니다."
            }
        } catch (e: Exception) {
            errorMessage = "평면도 정보를 불러오지 못했습니다."
        } finally {
            isLoading = false
        }
    }

    if (popupSensor != null) {
        AlertDialog(
            onDismissRequest = {
                popupSensor = null
                popupLatest = null
                popupLoading = false
            },
            title = {
                Text(popupSensor?.senName ?: "센서 정보")
            },
            text = {
                when {
                    popupLoading -> {
                        Text("최신 온습도 데이터를 불러오는 중입니다.")
                    }
                    popupLatest == null -> {
                        Column {
                            Text("온도: -")
                            Text("습도: -")
                            Text("최근 수신: -")
                        }
                    }
                    else -> {
                        Column {
                            Text("온도: ${popupLatest?.temp ?: "-"}°C")
                            Text("습도: ${popupLatest?.humid ?: "-"}%")
                            Text("최근 수신: ${popupLatest?.time ?: "-"}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        popupSensor = null
                        popupLatest = null
                        popupLoading = false
                    }
                ) {
                    Text("닫기")
                }
            }
        )
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                .background(Color(0xFFF7F7F7), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                bitmap == null -> {
                    Text(
                        text = "등록된 평면도가 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .onSizeChanged { imageSize = it }
                    ) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "평면도 미리보기",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        placedSensors.forEach { sensor ->
                            val x = (sensor.xRatio * imageSize.width).toInt()
                            val y = (sensor.yRatio * imageSize.height).toInt()

                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            x - 12,
                                            y - 12
                                        )
                                    }
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1976D2))
                                    .clickable {
                                        popupSensor = sensor
                                        popupLatest = null
                                        popupLoading = true

                                        scope.launch {
                                            try {
                                                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                                                val service = RetrofitClient.createService(baseUrl)
                                                val latestResponse = service.getLatestTempSensorValue(sensor.sensorId)

                                                if (latestResponse.status == "success") {
                                                    popupLatest = latestResponse.data
                                                } else {
                                                    popupLatest = null
                                                }
                                            } catch (e: Exception) {
                                                popupLatest = null
                                            } finally {
                                                popupLoading = false
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "T",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}