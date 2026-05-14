package com.example.ds_safer.ui.screens.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.domain.model.DashboardCctvDto
import com.example.ds_safer.domain.model.DashboardSensorDto
import com.example.ds_safer.domain.model.DashboardWorkerDto
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.domain.model.RecentAlertDto
import com.example.ds_safer.ui.theme.OnSafeBlueBrush
import com.example.ds_safer.ui.theme.OnSafeBottomBar
import com.example.ds_safer.ui.theme.OnSafeCard
import com.example.ds_safer.ui.theme.OnSafeColor
import com.example.ds_safer.ui.theme.OnSafeScreenBrush
import com.example.ds_safer.ui.theme.OnSafeSmallPill
import com.example.ds_safer.ui.theme.OnSafeStatusDot
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JetsonDetailScreen(
    device: JetsonDevice,
    userName: String,
    onDisconnectClick: () -> Unit,
    onNavigateToSensorRegister: () -> Unit,
    onNavigateToCctvRegister: () -> Unit,
    onNavigateToFloorMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var activeSheet by remember { mutableStateOf<DetailSheetType?>(null) }
    var showDisconnectDialog by remember { mutableStateOf(false) }
    var isDisconnecting by remember { mutableStateOf(false) }
    var disconnectError by remember { mutableStateOf<String?>(null) }

    // 대시보드 요약 상태
    var summary by remember { mutableStateOf<com.example.ds_safer.domain.model.DashboardSummaryData?>(null) }
    var summaryLoading by remember { mutableStateOf(false) }
    var summaryError by remember { mutableStateOf<String?>(null) }

    // 상세 목록 상태
    var recentAlerts by remember { mutableStateOf<List<RecentAlertDto>>(emptyList()) }
    var dashboardSensors by remember { mutableStateOf<List<DashboardSensorDto>>(emptyList()) }
    var dashboardCctvs by remember { mutableStateOf<List<DashboardCctvDto>>(emptyList()) }
    var dashboardWorkers by remember { mutableStateOf<List<DashboardWorkerDto>>(emptyList()) }

    androidx.compose.runtime.LaunchedEffect(device.ipAddress, device.port, device.spaceId) {
        val spaceId = device.spaceId
        if (spaceId == null || spaceId <= 0) {
            summaryError = "공간 정보가 없습니다."
            return@LaunchedEffect
        }
        val service = RetrofitClient.createService("http://${device.ipAddress}:${device.port}/")
        try {
            summaryLoading = true
            summaryError = null
            val response = service.getDashboardSummary(spaceId)
            val ok = response.success == true || response.status == "success"
            if (ok && response.data != null) summary = response.data
            else summaryError = "대시보드 정보를 불러오지 못했습니다."
        } catch (e: Exception) {
            summaryError = "대시보드 정보를 불러오지 못했습니다."
        } finally {
            summaryLoading = false
        }
        try {
            recentAlerts = service.getRecentAlerts(spaceId, 20).data
        } catch (_: Exception) {}
        try {
            dashboardSensors = service.getDashboardSensors(spaceId).data
        } catch (_: Exception) {}
        try {
            dashboardCctvs = service.getDashboardCctvs(spaceId).data
        } catch (_: Exception) {}
        try {
            dashboardWorkers = service.getDashboardWorkers(spaceId).data
        } catch (_: Exception) {}
    }

    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDisconnecting) {
                    showDisconnectDialog = false
                }
            },
            containerColor = OnSafeColor.Card,
            titleContentColor = OnSafeColor.TextPrimary,
            textContentColor = OnSafeColor.TextSecondary,
            title = {
                Text("Jetson 등록 해제")
            },
            text = {
                Column {
                    Text("이 Jetson을 DB 등록 목록에서 삭제하시겠습니까?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("삭제 후에도 Jetson 서버가 실행 중이면 mDNS 등록 후보에는 다시 표시됩니다.")

                    disconnectError?.let {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = it,
                            color = OnSafeColor.Red
                        )
                    }

                    if (isDisconnecting) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = OnSafeColor.Blue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("등록 해제 중입니다.")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isDisconnecting,
                    onClick = {
                        val jetsonId = device.jetsonId

                        if (jetsonId == null || jetsonId <= 0) {
                            disconnectError = "DB에 등록된 Jetson ID가 없습니다."
                            return@TextButton
                        }

                        scope.launch {
                            try {
                                isDisconnecting = true
                                disconnectError = null

                                val service = RetrofitClient.createService(
                                    "http://${device.ipAddress}:${device.port}/"
                                )

                                val response = service.deleteJetsonV1(jetsonId)

                                if (response.success) {
                                    showDisconnectDialog = false
                                    onDisconnectClick()
                                } else {
                                    disconnectError = response.message.ifBlank {
                                        "Jetson 등록 해제에 실패했습니다."
                                    }
                                }
                            } catch (e: HttpException) {
                                disconnectError = "Jetson 등록 해제 실패: HTTP ${e.code()}"
                            } catch (e: Exception) {
                                disconnectError = "Jetson 등록 해제 실패: ${e.message}"
                            } finally {
                                isDisconnecting = false
                            }
                        }
                    }
                ) {
                    Text("해제", color = OnSafeColor.Red)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDisconnecting,
                    onClick = {
                        showDisconnectDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        containerColor = OnSafeColor.BgTop,
        bottomBar = {
            OnSafeBottomBar(
                selected = "홈",
                onHomeClick = {},
                onFloorMapClick = onNavigateToFloorMap,
                onProfileClick = onNavigateToProfile
            )
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
                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${userName}님,\n안전한 하루 되세요! 👷",
                            color = OnSafeColor.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OnSafeStatusDot(
                                color = OnSafeColor.Green,
                                text = "연결된 현장"
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "${device.name} · ${device.spaceName ?: "공간 미지정"}",
                                color = OnSafeColor.TextSecondary,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(onClick = { activeSheet = DetailSheetType.ALERTS }) {
                        val alertCount = summary?.dangerAlertCount ?: 0
                        BadgedBox(
                            badge = {
                                if (alertCount > 0) {
                                    Badge(
                                        containerColor = OnSafeColor.Red,
                                        contentColor = Color.White
                                    ) {
                                        Text(if (alertCount > 9) "9+" else alertCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "알림",
                                tint = OnSafeColor.TextPrimary
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Warning,
                        iconColor = OnSafeColor.Red,
                        title = "위험 알림",
                        value = "${summary?.dangerAlertCount ?: 0}건",
                        sub = "미확인 알림",
                        onClick = { activeSheet = DetailSheetType.ALERTS }
                    )

                    DashboardMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Build,
                        iconColor = OnSafeColor.Blue,
                        title = "센서",
                        value = "${summary?.sensorTotal ?: 0}개",
                        sub = "등록됨",
                        onClick = { activeSheet = DetailSheetType.SENSORS }
                    )

                    DashboardMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Add,
                        iconColor = OnSafeColor.Blue,
                        title = "CCTV",
                        value = "${summary?.cctvTotal ?: 0}대",
                        sub = "등록됨",
                        onClick = { activeSheet = DetailSheetType.CCTVS }
                    )

                    DashboardMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.LocationOn,
                        iconColor = OnSafeColor.Green,
                        title = "작업자",
                        value = "${summary?.workerTotal ?: 0}명",
                        sub = "해당 공간",
                        onClick = { activeSheet = DetailSheetType.WORKERS }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "현장 관리",
                    color = OnSafeColor.TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
            }

            item {
                ActionMenuCard(
                    icon = Icons.Default.Build,
                    title = "센서 등록하기",
                    description = "온습도, 심박 센서를 현재 현장에 등록합니다.",
                    onClick = onNavigateToSensorRegister
                )
            }

            item {
                ActionMenuCard(
                    icon = Icons.Default.Add,
                    title = "CCTV 등록하기",
                    description = "현재 공간에 CCTV를 등록하고 검증합니다.",
                    onClick = onNavigateToCctvRegister
                )
            }

            item {
                ActionMenuCard(
                    icon = Icons.Default.LocationOn,
                    title = "현장 평면도",
                    description = "space_id 기준 평면도와 센서 위치를 확인합니다.",
                    onClick = onNavigateToFloorMap
                )
            }

            item {
                OutlinedButton(
                    onClick = {
                        disconnectError = null
                        showDisconnectDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, OnSafeColor.Red),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OnSafeColor.Red
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("이 Jetson 등록 해제")
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = sheetState,
            containerColor = OnSafeColor.Card
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp, max = 700.dp)
                    .padding(horizontal = 20.dp)
            ) {
                val sheetTitle = when (activeSheet) {
                    DetailSheetType.ALERTS -> "최근 알림"
                    DetailSheetType.SENSORS -> "등록된 센서"
                    DetailSheetType.CCTVS -> "등록된 CCTV"
                    DetailSheetType.WORKERS -> "등록된 작업자"
                    null -> ""
                }
                Text(
                    text = sheetTitle,
                    color = OnSafeColor.TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                when (activeSheet) {
                    DetailSheetType.ALERTS -> AlertsSheetContent(recentAlerts)
                    DetailSheetType.SENSORS -> SensorsSheetContent(dashboardSensors)
                    DetailSheetType.CCTVS -> CctvsSheetContent(dashboardCctvs)
                    DetailSheetType.WORKERS -> WorkersSheetContent(dashboardWorkers)
                    null -> {}
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

enum class DetailSheetType { ALERTS, SENSORS, CCTVS, WORKERS }

@Composable
private fun AlertsSheetContent(alerts: List<RecentAlertDto>) {
    if (alerts.isEmpty()) {
        EmptySheetMessage("최근 알림이 없습니다.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(alerts) { alert ->
            val isRed = alert.level == "danger"
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = (if (isRed) OnSafeColor.Red else OnSafeColor.Blue).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isRed) OnSafeColor.Red else OnSafeColor.Blue,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alert.title ?: "알림",
                            color = OnSafeColor.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = alert.message ?: "",
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!alert.cameraName.isNullOrBlank()) {
                            Text(
                                text = "카메라: ${alert.cameraName}",
                                color = OnSafeColor.TextTertiary,
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Text(
                        text = alert.createdAt?.takeLast(8)?.take(5) ?: "",
                        color = OnSafeColor.TextTertiary,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SensorsSheetContent(sensors: List<DashboardSensorDto>) {
    if (sensors.isEmpty()) {
        EmptySheetMessage("등록된 센서가 없습니다.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sensors) { sensor ->
            val online = sensor.isOnline == 1
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = OnSafeColor.CardSoft
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (online) OnSafeColor.Green else OnSafeColor.Gray,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sensor.senName ?: sensor.sensorId ?: "센서",
                            color = OnSafeColor.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${sensor.sensorType ?: ""} · ${sensor.senLocate ?: ""}",
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                        sensor.lastSeenAt?.let {
                            Text(
                                text = "마지막 수신: $it",
                                color = OnSafeColor.TextTertiary,
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Text(
                        text = if (online) "온라인" else "오프라인",
                        color = if (online) OnSafeColor.Green else OnSafeColor.Gray,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun CctvsSheetContent(cctvs: List<DashboardCctvDto>) {
    if (cctvs.isEmpty()) {
        EmptySheetMessage("등록된 CCTV가 없습니다.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(cctvs) { cctv ->
            val online = cctv.isOnline == 1
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = OnSafeColor.CardSoft
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (online) OnSafeColor.Green else OnSafeColor.Gray,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cctv.senName ?: cctv.ipAddress ?: "CCTV",
                            color = OnSafeColor.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "ID: ${cctv.cameraId ?: ""} · IP: ${cctv.ipAddress ?: ""}",
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = if (online) "온라인" else "오프라인",
                        color = if (online) OnSafeColor.Green else OnSafeColor.Gray,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkersSheetContent(workers: List<DashboardWorkerDto>) {
    if (workers.isEmpty()) {
        EmptySheetMessage("등록된 작업자가 없습니다.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(workers) { worker ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = OnSafeColor.CardSoft
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(OnSafeColor.Blue.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = worker.name?.take(1) ?: "?",
                            color = OnSafeColor.Blue,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = worker.name ?: "작업자",
                            color = OnSafeColor.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "사번: ${worker.deptId ?: "-"} · ${worker.sensorType ?: ""}",
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                        worker.sensorName?.let {
                            Text(
                                text = "센서: $it",
                                color = OnSafeColor.TextTertiary,
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySheetMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = OnSafeColor.TextSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun HeroSafetyCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        shape = RoundedCornerShape(22.dp),
        color = OnSafeColor.Blue
    ) {
        Box(
            modifier = Modifier
                .background(OnSafeBlueBrush)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    text = "실시간 모니터링 정상",
                    color = Color.White.copy(alpha = 0.82f),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "모든 구역이\n안정합니다",
                    color = Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = OnSafeColor.Card.copy(alpha = 0.35f)
                ) {
                    Text(
                        text = "자세히 보기 〉",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                text = "🛡",
                modifier = Modifier.align(Alignment.CenterEnd),
                style = androidx.compose.material3.MaterialTheme.typography.displayLarge
            )
        }
    }
}

@Composable
private fun DashboardMiniCard(
    modifier: Modifier,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    sub: String,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        modifier.height(136.dp).clickable { onClick() }
    } else {
        modifier.height(136.dp)
    }

    OnSafeCard(
        modifier = cardModifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = title,
            color = OnSafeColor.TextPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = value,
            color = OnSafeColor.TextPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )

        Text(
            text = sub,
            color = OnSafeColor.TextSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ActionMenuCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    OnSafeCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSafeColor.Blue,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = OnSafeColor.TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = OnSafeColor.TextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = OnSafeColor.TextSecondary
            )
        }
    }
}

@Composable
private fun RecentActivitySection() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "최근 활동",
                color = OnSafeColor.TextPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "더보기 〉",
                color = OnSafeColor.TextSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OnSafeCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            ActivityRow(
                iconColor = OnSafeColor.Red,
                title = "화재감지기(2층 창고) 경고 해제",
                desc = "정상으로 복구되었습니다.",
                time = "09:12",
                badge = "정보"
            )

            Divider(
                color = OnSafeColor.StrokeSoft,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            ActivityRow(
                iconColor = OnSafeColor.Blue,
                title = "가스센서 점검 완료",
                desc = "1층 보일러실 · CO 센서 점검 완료",
                time = "08:45",
                badge = "정보"
            )
        }
    }
}

@Composable
private fun ColumnScope.ActivityRow(
    iconColor: Color,
    title: String,
    desc: String,
    time: String,
    badge: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = iconColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = iconColor,
                        shape = RoundedCornerShape(50)
                    )
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
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
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
                color = if (badge == "경고") {
                    OnSafeColor.Orange
                } else {
                    OnSafeColor.Blue
                }
            )
        }
    }
}