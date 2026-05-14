package com.example.ds_safer.ui.screens.main

import AuthViewModel
import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
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
    onNavigateToJetsonSpaceRegister: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val registeredList by viewModel.registeredJetsons.collectAsState()
    val isLoadingRegisteredJetsons by viewModel.isLoadingRegisteredJetsons.collectAsState()

    val context = LocalContext.current

    var alertHistory by remember { mutableStateOf(listOf<HazardAlert>()) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("Permission", "알림 권한 허용됨")
        } else {
            Log.e("Permission", "알림 권한 거부됨")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadRegisteredJetsonsFromDiscoveredServers()
    }

    LaunchedEffect(registeredList) {
        val firstJetson = registeredList.firstOrNull()
        if (firstJetson != null) {
            WebSocketManager.connect(firstJetson.ipAddress)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        WebSocketManager.alertFlow.collect { alert ->
            alertHistory = listOf(alert) + alertHistory
            WebSocketManager.sendSystemPushNotification(context, alert)

            if (alert.vibration) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        1000,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("기기 연결") },
                actions = {
                    IconButton(onClick = onNavigateToJetsonSpaceRegister) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Jetson 공간 등록"
                        )
                    }

                    IconButton(onClick = { showNotificationSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (alertHistory.isNotEmpty()) {
                                    Badge {
                                        Text(alertHistory.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "알림함"
                            )
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
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "등록된 Jetson",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    if (isLoadingRegisteredJetsons) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            if (registeredList.isEmpty()) {
                item {
                    EmptyRegisteredJetsonCard(
                        onRegisterClick = onNavigateToJetsonSpaceRegister
                    )
                }
            } else {
                items(registeredList) { device ->
                    RegisteredJetsonDeviceItem(
                        device = device,
                        onClick = {
                            onNavigateToDetail(device)
                        }
                    )
                }
            }
        }
    }

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
                                        showNotificationSheet = false
                                        onNavigateToReport(alert.eventId.toInt())
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                )
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
private fun RegisteredJetsonDeviceItem(
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
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "IP: ${device.ipAddress} | Port: ${device.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Text(
                    text = "등록 공간: ${device.spaceName ?: "미지정"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (device.status) "상태: 활성화" else "상태: 비활성화",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (device.status) Color(0xFF2E7D32) else Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "상세 이동",
                tint = Color.Gray
            )
        }
    }
}

@Composable
private fun EmptyRegisteredJetsonCard(
    onRegisterClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "등록된 Jetson이 없습니다.",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "오른쪽 상단 + 버튼을 눌러 mDNS로 발견된 Jetson과 공간을 매칭해서 등록해주세요.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Jetson 공간 등록하기")
            }
        }
    }
}