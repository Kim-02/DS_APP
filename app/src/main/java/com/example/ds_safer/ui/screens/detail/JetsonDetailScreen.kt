package com.example.ds_safer.ui.screens.detail

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.AlertRepository
import com.example.ds_safer.data.websocket.WebSocketManager
import com.example.ds_safer.domain.model.DashboardCctvDto
import com.example.ds_safer.domain.model.DashboardSensorDto
import com.example.ds_safer.domain.model.DashboardWorkerDto
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.domain.model.RecentAlertDto
import com.example.ds_safer.ui.theme.OnSafeBottomBar
import com.example.ds_safer.ui.theme.OnSafeCard
import com.example.ds_safer.ui.theme.OnSafeColor
import com.example.ds_safer.ui.theme.OnSafeScreenBrush
import com.example.ds_safer.ui.theme.OnSafeSmallPill
import com.example.ds_safer.ui.theme.OnSafeStatusDot
import com.example.ds_safer.util.NotificationHelper
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
    onNavigateToScenarioVideo: () -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var activeSheet by remember { mutableStateOf<DetailSheetType?>(null) }
    var showDisconnectDialog by remember { mutableStateOf(false) }
    var isDisconnecting by remember { mutableStateOf(false) }
    var disconnectError by remember { mutableStateOf<String?>(null) }
    var selectedAlert by remember { mutableStateOf<RecentAlertDto?>(null) }

    // 대시보드 요약 상태 (센서/CCTV/작업자 카운트)
    var summary by remember { mutableStateOf<com.example.ds_safer.domain.model.DashboardSummaryData?>(null) }
    var summaryLoading by remember { mutableStateOf(false) }
    var summaryError by remember { mutableStateOf<String?>(null) }

    var dashboardSensors by remember { mutableStateOf<List<DashboardSensorDto>>(emptyList()) }
    var dashboardCctvs by remember { mutableStateOf<List<DashboardCctvDto>>(emptyList()) }
    var dashboardWorkers by remember { mutableStateOf<List<DashboardWorkerDto>>(emptyList()) }

    // AlertRepository StateFlow → UI 자동 갱신
    val recentAlerts by AlertRepository.alerts.collectAsState()
    val unreadCount = recentAlerts.count { it.isRead != true }

    // JetsonDetailScreen 진입 시 selectedJetson 기준으로 WebSocket 연결 보장
    LaunchedEffect(device.ipAddress, device.port) {
        android.util.Log.d("WS_APP", "JetsonDetailScreen: ensuring connect ${device.ipAddress}:${device.port}")
        WebSocketManager.connect(device.ipAddress, device.port)
    }

    // 초기 데이터 로드
    LaunchedEffect(device.ipAddress, device.port, device.spaceId) {
        val spaceId = device.spaceId
        if (spaceId == null || spaceId <= 0) {
            summaryError = "공간 정보가 없습니다."
            return@LaunchedEffect
        }
        val service = RetrofitClient.createService("http://${device.ipAddress}:${device.port}/")

        summaryLoading = true
        summaryError = null
        try {
            val resp = service.getDashboardSummary(spaceId)
            if ((resp.success == true || resp.status == "success") && resp.data != null) {
                summary = resp.data
            } else {
                summaryError = "대시보드 정보를 불러오지 못했습니다."
            }
        } catch (_: Exception) {
            summaryError = "대시보드 정보를 불러오지 못했습니다."
        } finally {
            summaryLoading = false
        }

        try {
            val fetched = service.getRecentAlerts(spaceId, 20).data
            AlertRepository.setAlerts(fetched)
        } catch (_: Exception) {}

        try { dashboardSensors = service.getDashboardSensors(spaceId).data } catch (_: Exception) {}
        try { dashboardCctvs  = service.getDashboardCctvs(spaceId).data  } catch (_: Exception) {}
        try { dashboardWorkers = service.getDashboardWorkers(spaceId).data } catch (_: Exception) {}
    }

    // WebSocket hazard_alert 수신 → 푸시 알림 (AlertRepository 갱신은 WebSocketManager에서 이미 처리)
    LaunchedEffect(device.spaceId) {
        WebSocketManager.alertFlow.collect { alert ->
            android.util.Log.d("WS_APP", "JetsonDetailScreen: alertFlow received event_id=${alert.eventId} space_id=${alert.spaceId} device.spaceId=${device.spaceId}")
            // space_id가 null이거나 현재 화면과 일치하는 경우만 알림 표시
            val alertSpace = alert.spaceId
            if (alertSpace == null || alertSpace == device.spaceId) {
                android.util.Log.d("NOTI", "showHazardNotification event_id=${alert.eventId} title=${alert.title}")
                NotificationHelper.showHazardNotification(
                    context = context,
                    eventId = alert.eventId?.toInt(),
                    title   = alert.title ?: alert.evCodeName ?: "위험 감지 알림",
                    message = alert.message ?: "위험이 감지되었습니다.",
                    level   = alert.level,
                )
            }
        }
    }

    // ── 읽음 처리 함수 ─────────────────────────────────────────────────────
    fun markAsRead(alert: RecentAlertDto) {
        val eventId = alert.eventId ?: return
        if (alert.isRead == true) return
        AlertRepository.markAsRead(eventId)           // 로컬 즉시 반영
        scope.launch {
            try {
                val service = RetrofitClient.createService(
                    "http://${device.ipAddress}:${device.port}/"
                )
                service.markAlertAsRead(eventId, device.spaceId)
            } catch (_: Exception) { /* 서버 실패 시 로컬 반영만 유지 */ }
        }
    }

    // ── 연결 해제 다이얼로그 ──────────────────────────────────────────────
    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDisconnecting) showDisconnectDialog = false },
            containerColor = OnSafeColor.Card,
            titleContentColor = OnSafeColor.TextPrimary,
            textContentColor = OnSafeColor.TextSecondary,
            title = { Text("Jetson 등록 해제") },
            text = {
                Column {
                    Text("이 Jetson을 DB 등록 목록에서 삭제하시겠습니까?")
                    Spacer(Modifier.height(8.dp))
                    Text("삭제 후에도 Jetson 서버가 실행 중이면 mDNS 등록 후보에는 다시 표시됩니다.")
                    disconnectError?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(it, color = OnSafeColor.Red)
                    }
                    if (isDisconnecting) {
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = OnSafeColor.Blue,
                            )
                            Spacer(Modifier.width(8.dp))
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
                    },
                ) { Text("해제", color = OnSafeColor.Red) }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDisconnecting,
                    onClick = { showDisconnectDialog = false },
                ) { Text("취소") }
            },
        )
    }

    // ── 알림 상세 모달 ────────────────────────────────────────────────────
    selectedAlert?.let { alert ->
        AlertDialog(
            onDismissRequest = { selectedAlert = null },
            containerColor = OnSafeColor.Card,
            titleContentColor = OnSafeColor.TextPrimary,
            textContentColor = OnSafeColor.TextSecondary,
            title = { Text(alert.title ?: "알림 상세") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val levelColor = alertLevelColor(alert.level)
                    OnSafeSmallPill(
                        text = when (alert.level) {
                            "danger"  -> "위험"
                            "warning" -> "주의"
                            "info"    -> "정보"
                            else      -> alert.level ?: "-"
                        },
                        color = levelColor,
                    )
                    if (!alert.createdAt.isNullOrBlank()) {
                        Text("발생 시간: ${alert.createdAt}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = OnSafeColor.TextSecondary)
                    }
                    if (!alert.cameraName.isNullOrBlank()) {
                        Text("카메라: ${alert.cameraName}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = OnSafeColor.TextSecondary)
                    }
                    if (!alert.source.isNullOrBlank()) {
                        Text("출처: ${alert.source}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = OnSafeColor.TextSecondary)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = alert.message ?: "-",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = OnSafeColor.TextPrimary,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAlert = null }) {
                    Text("닫기", color = OnSafeColor.Blue)
                }
            },
        )
    }

    Scaffold(
        containerColor = OnSafeColor.BgTop,
        bottomBar = {
            OnSafeBottomBar(
                selected = "홈",
                onHomeClick = {},
                onFloorMapClick = onNavigateToFloorMap,
                onProfileClick = onNavigateToProfile,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(OnSafeScreenBrush)
                .padding(padding)
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${userName}님,\n안전한 하루 되세요! 👷",
                            color = OnSafeColor.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OnSafeStatusDot(color = OnSafeColor.Green, text = "연결된 현장")
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${device.name} · ${device.spaceName ?: "공간 미지정"}",
                                color = OnSafeColor.TextSecondary,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    // 종모양 – unread count 배지
                    IconButton(onClick = { activeSheet = DetailSheetType.ALERTS }) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = OnSafeColor.Red,
                                        contentColor = Color.White,
                                    ) {
                                        Text(if (unreadCount > 9) "9+" else unreadCount.toString())
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "알림",
                                tint = OnSafeColor.TextPrimary,
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
                        value = "${unreadCount}건",
                        sub = "미확인 알림",
                        onClick = { activeSheet = DetailSheetType.ALERTS },
                    )
                    DashboardMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Build,
                        iconColor = OnSafeColor.Blue,
                        title = "센서",
                        value = "${summary?.sensorTotal ?: 0}개",
                        sub = "등록됨",
                        onClick = { activeSheet = DetailSheetType.SENSORS },
                    )
                    DashboardMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Add,
                        iconColor = OnSafeColor.Blue,
                        title = "CCTV",
                        value = "${summary?.cctvTotal ?: 0}대",
                        sub = "등록됨",
                        onClick = { activeSheet = DetailSheetType.CCTVS },
                    )
                    DashboardMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.LocationOn,
                        iconColor = OnSafeColor.Green,
                        title = "작업자",
                        value = "${summary?.workerTotal ?: 0}명",
                        sub = "해당 공간",
                        onClick = { activeSheet = DetailSheetType.WORKERS },
                    )
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "현장 관리",
                    color = OnSafeColor.TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                )
            }

            item {
                ActionMenuCard(
                    icon = Icons.Default.Build,
                    title = "센서 등록하기",
                    description = "온습도, 심박 센서를 현재 현장에 등록합니다.",
                    onClick = onNavigateToSensorRegister,
                )
            }
            item {
                ActionMenuCard(
                    icon = Icons.Default.Add,
                    title = "CCTV 등록하기",
                    description = "현재 공간에 CCTV를 등록하고 검증합니다.",
                    onClick = onNavigateToCctvRegister,
                )
            }
            item {
                ActionMenuCard(
                    icon = Icons.Default.LocationOn,
                    title = "현장 평면도",
                    description = "space_id 기준 평면도와 센서 위치를 확인합니다.",
                    onClick = onNavigateToFloorMap,
                )
            }
            item {
                ScenarioCard(onClick = onNavigateToScenarioVideo)
            }

            item {
                OutlinedButton(
                    onClick = {
                        disconnectError = null
                        showDisconnectDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, OnSafeColor.Red),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSafeColor.Red),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("이 Jetson 등록 해제")
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    // ── BottomSheet ───────────────────────────────────────────────────────
    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = sheetState,
            containerColor = OnSafeColor.Card,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp, max = 700.dp)
                    .padding(horizontal = 20.dp),
            ) {
                val sheetTitle = when (activeSheet) {
                    DetailSheetType.ALERTS  -> "최근 알림"
                    DetailSheetType.SENSORS -> "등록된 센서"
                    DetailSheetType.CCTVS   -> "등록된 CCTV"
                    DetailSheetType.WORKERS -> "등록된 작업자"
                    null -> ""
                }
                Text(
                    text = sheetTitle,
                    color = OnSafeColor.TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp),
                )

                when (activeSheet) {
                    DetailSheetType.ALERTS -> AlertsSheetContent(
                        alerts = recentAlerts,
                        onAlertClick = { alert ->
                            selectedAlert = alert
                            markAsRead(alert)
                        },
                    )
                    DetailSheetType.SENSORS -> SensorsSheetContent(dashboardSensors)
                    DetailSheetType.CCTVS   -> CctvsSheetContent(dashboardCctvs)
                    DetailSheetType.WORKERS -> WorkersSheetContent(dashboardWorkers)
                    null -> {}
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

enum class DetailSheetType { ALERTS, SENSORS, CCTVS, WORKERS }

// ─────────────────────────────────────────────────────────────────────────────
// Composable helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun alertLevelColor(level: String?): Color = when (level) {
    "danger"  -> Color(0xFFE53935)
    "warning" -> Color(0xFFFF9800)
    "info"    -> Color(0xFF1E88E5)
    else      -> Color(0xFFE53935)
}

@Composable
private fun AlertsSheetContent(
    alerts: List<RecentAlertDto>,
    onAlertClick: (RecentAlertDto) -> Unit,
) {
    if (alerts.isEmpty()) {
        EmptySheetMessage("최근 알림이 없습니다.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(alerts) { alert ->
            val isUnread = alert.isRead != true
            val levelColor = alertLevelColor(alert.level)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAlertClick(alert) },
                shape = RoundedCornerShape(12.dp),
                color = if (isUnread) levelColor.copy(alpha = 0.12f)
                        else OnSafeColor.CardSoft,
                border = if (isUnread) androidx.compose.foundation.BorderStroke(1.dp, levelColor.copy(alpha = 0.4f))
                         else null,
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isUnread) levelColor else OnSafeColor.Gray,
                                CircleShape,
                            ),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alert.title ?: "알림",
                            color = if (isUnread) OnSafeColor.TextPrimary else OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = alert.message ?: "",
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!alert.cameraName.isNullOrBlank()) {
                            Text(
                                text = "카메라: ${alert.cameraName}",
                                color = OnSafeColor.TextTertiary,
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = alert.createdAt?.takeLast(8)?.take(5) ?: "",
                        color = OnSafeColor.TextTertiary,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun SensorsSheetContent(sensors: List<DashboardSensorDto>) {
    if (sensors.isEmpty()) { EmptySheetMessage("등록된 센서가 없습니다."); return }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sensors) { sensor ->
            val online = sensor.isOnline == 1
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = OnSafeColor.CardSoft,
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(if (online) OnSafeColor.Green else OnSafeColor.Gray, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sensor.senName ?: sensor.sensorId ?: "센서", color = OnSafeColor.TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                        Text("${sensor.sensorType ?: ""} · ${sensor.senLocate ?: ""}", color = OnSafeColor.TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        sensor.lastSeenAt?.let { Text("마지막 수신: $it", color = OnSafeColor.TextTertiary, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) }
                    }
                    Text(if (online) "온라인" else "오프라인", color = if (online) OnSafeColor.Green else OnSafeColor.Gray, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun CctvsSheetContent(cctvs: List<DashboardCctvDto>) {
    if (cctvs.isEmpty()) { EmptySheetMessage("등록된 CCTV가 없습니다."); return }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(cctvs) { cctv ->
            val online = cctv.isOnline == 1
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = OnSafeColor.CardSoft) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(if (online) OnSafeColor.Green else OnSafeColor.Gray, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cctv.senName ?: cctv.ipAddress ?: "CCTV", color = OnSafeColor.TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                        Text("ID: ${cctv.cameraId ?: ""} · IP: ${cctv.ipAddress ?: ""}", color = OnSafeColor.TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                    }
                    Text(if (online) "온라인" else "오프라인", color = if (online) OnSafeColor.Green else OnSafeColor.Gray, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun WorkersSheetContent(workers: List<DashboardWorkerDto>) {
    if (workers.isEmpty()) { EmptySheetMessage("등록된 작업자가 없습니다."); return }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(workers) { worker ->
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = OnSafeColor.CardSoft) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).background(OnSafeColor.Blue.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                        Text(worker.name?.take(1) ?: "?", color = OnSafeColor.Blue, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(worker.name ?: "작업자", color = OnSafeColor.TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                        Text("사번: ${worker.deptId ?: "-"} · ${worker.sensorType ?: ""}", color = OnSafeColor.TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        worker.sensorName?.let { Text("센서: $it", color = OnSafeColor.TextTertiary, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySheetMessage(message: String) {
    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
        Text(text = message, color = OnSafeColor.TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
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
    onClick: (() -> Unit)? = null,
) {
    val cardModifier = if (onClick != null) modifier.height(136.dp).clickable { onClick() }
                       else modifier.height(136.dp)
    OnSafeCard(modifier = cardModifier) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(10.dp))
        Text(title, color = OnSafeColor.TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(6.dp))
        Text(value, color = OnSafeColor.TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        Text(sub, color = OnSafeColor.TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ActionMenuCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    OnSafeCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = OnSafeColor.Blue, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = OnSafeColor.TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(description, color = OnSafeColor.TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = OnSafeColor.TextSecondary)
        }
    }
}

@Composable
private fun ScenarioCard(onClick: () -> Unit) {
    OnSafeCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(OnSafeColor.Red.copy(alpha = 0.12f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = OnSafeColor.Red,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "시나리오 3",
                    color = OnSafeColor.Red,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "화재 위험 상황 영상 시연",
                    color = OnSafeColor.TextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = OnSafeColor.Red.copy(alpha = 0.6f),
            )
        }
    }
}
