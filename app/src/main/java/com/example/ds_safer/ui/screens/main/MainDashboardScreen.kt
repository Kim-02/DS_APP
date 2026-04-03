package com.example.ds_safer.ui.screens.main

import AuthViewModel
import android.Manifest // 🌟 권한용 임포트
import android.content.Context
import android.os.Build // 🌟 버전 체크용 임포트
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log // 🌟 로그용 임포트
import androidx.activity.compose.rememberLauncherForActivityResult // 🌟 권한 런처 임포트
import androidx.activity.result.contract.ActivityResultContracts // 🌟 권한 런처 임포트
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ds_safer.data.websocket.WebSocketManager
import com.example.ds_safer.domain.model.HazardAlert
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.ui.screens.discovery.DiscoveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: DiscoveryViewModel,
    authViewModel: AuthViewModel,
    onNavigateToDetail: (JetsonDevice) -> Unit,
    onNavigateToReport: (Int) -> Unit,
    onLogoutClick: () -> Unit
) {
    val registeredList by viewModel.registeredJetsons.collectAsState()
    val discoveredList by viewModel.discoveredJetsons.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val context = LocalContext.current

    // 🌟 1. 알림 기록 저장용 리스트 & 바텀 시트 상태
    var alertHistory by remember { mutableStateOf(listOf<HazardAlert>()) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // 🌟 2. 알림 권한 요청 런처 (안드로이드 13 이상용)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) Log.d("Permission", "알림 권한 허용됨!")
        else Log.e("Permission", "알림 권한 거부됨 ㅠㅠ 상단 알림이 안 뜹니다.")
    }

    // 🌟 3. 웹소켓 자동 연결 (등록된 기기가 있으면 그 IP로 연결)
    LaunchedEffect(registeredList) {
        val firstJetson = registeredList.firstOrNull()
        if (firstJetson != null) {
            WebSocketManager.connect(firstJetson.ipAddress)
        }
    }

    // 🌟 4. 알림 권한 요청 및 웹소켓 알림 수신 대기
    LaunchedEffect(Unit) {
        // 화면 켜질 때 안드로이드 13(TIRAMISU) 이상이면 알림 권한 팝업 띄우기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 웹소켓에서 날아오는 알림 데이터 수집
        WebSocketManager.alertFlow.collect { alert ->
            // 알림 리스트 맨 앞에 새 알림 추가
            alertHistory = listOf(alert) + alertHistory

            // 상단 시스템 푸시 알림 띄우기
            WebSocketManager.sendSystemPushNotification(context, alert)

            // 추가 진동 울리기
            if (alert.vibration) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("기기 연결") },
                actions = {
                    // 🌟 5. 종 모양 알림함 (알림이 있으면 빨간 뱃지 표시)
                    IconButton(onClick = { showNotificationSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (alertHistory.isNotEmpty()) {
                                    Badge { Text(alertHistory.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "알림함")
                        }
                    }

                    TextButton(onClick = onLogoutClick) {
                        Text("로그아웃", color = Color.Red)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ==========================================
            // Section 1: 등록된 기기 (내 기기)
            // ==========================================
            item {
                Text(
                    text = "등록된 기기",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            if (registeredList.isEmpty()) {
                item {
                    Text(
                        "등록된 기기가 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            } else {
                items(registeredList) { device ->
                    JetsonDeviceItem(
                        device = device,
                        onClick = { onNavigateToDetail(device) }
                    )
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

            // ==========================================
            // Section 2: 연결 가능한 기기 (새로 찾은 기기)
            // ==========================================
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "연결 가능한 기기",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    if (isScanning) {
                        Spacer(Modifier.width(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    }
                }
            }

            if (discoveredList.isEmpty()) {
                item {
                    Text("주변 기기를 찾는 중...", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                items(discoveredList) { device ->
                    JetsonDeviceItem(
                        device = device,
                        onClick = { viewModel.registerDevice(device) }
                    )
                }
            }
        }
    }

    // 🌟 6. 종 모양 아이콘을 누르면 올라오는 '알림 모아보기' 바텀 시트
    if (showNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("알림 기록", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                if (alertHistory.isEmpty()) {
                    Text("최근 발생한 위험 알림이 없습니다.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(32.dp))
                } else {
                    LazyColumn {
                        items(alertHistory) { alert ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        showNotificationSheet = false // 알림창 닫고
                                        onNavigateToReport(alert.eventId.toInt()) // 🌟 작성 화면으로 이동!
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "경고",
                                        tint = Color.Red,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = alert.message,
                                            color = Color.Red,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "카메라명: ${alert.cameraName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JetsonDeviceItem(
    device: JetsonDevice,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "IP: ${device.ipAddress} | Port: ${device.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "이동/연결",
                tint = Color.Gray
            )
        }
    }
}