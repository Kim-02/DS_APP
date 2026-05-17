package com.example.ds_safer.ui.screens.main

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ds_safer.data.repository.AlertRepository
import com.example.ds_safer.data.websocket.WebSocketManager
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.ui.screens.discovery.DiscoveryViewModel
import com.example.ds_safer.ui.theme.OnSafeCard
import com.example.ds_safer.ui.theme.OnSafeColor
import com.example.ds_safer.ui.theme.OnSafeOutlineButton
import com.example.ds_safer.ui.theme.OnSafePrimaryButton
import com.example.ds_safer.ui.theme.OnSafeScreenBrush
import com.example.ds_safer.ui.theme.OnSafeSmallPill
import com.example.ds_safer.ui.theme.OnSafeStatusDot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: DiscoveryViewModel,
    onNavigateToDetail: (JetsonDevice) -> Unit,
    onNavigateToJetsonSpaceRegister: () -> Unit,
    onNavigateToReport: (Int) -> Unit,
    onLogoutClick: () -> Unit
) {
    val registeredList by viewModel.registeredJetsons.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val alertHistory by AlertRepository.alerts.collectAsState()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        viewModel.loadRegisteredJetsonsFromDiscoveredServers()
    }

    LaunchedEffect(registeredList) {
        val first = registeredList.firstOrNull()
        if (first != null) {
            WebSocketManager.connect(first.ipAddress, first.port)
        }
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "등록된 Jetson",
                            color = OnSafeColor.TextPrimary,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "현장으로 이동하여 모니터링을 시작하세요.",
                            color = OnSafeColor.TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    IconButton(onClick = { showNotificationSheet = true }) {
                        val unreadCount = alertHistory.count { it.isRead != true }
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge {
                                        Text(unreadCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "알림함",
                                tint = OnSafeColor.TextPrimary
                            )
                        }
                    }

                    TextButton(onClick = onLogoutClick) {
                        Text("로그아웃", color = OnSafeColor.Red)
                    }
                }
            }

            if (registeredList.isEmpty()) {
                item {
                    EmptyJetsonCard(
                        isScanning = isScanning,
                        onRegisterClick = onNavigateToJetsonSpaceRegister
                    )
                }
            } else {
                items(registeredList) { jetson ->
                    RegisteredJetsonCard(
                        jetson = jetson,
                        primary = registeredList.firstOrNull() == jetson,
                        onClick = {
                            onNavigateToDetail(jetson)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OnSafeOutlineButton(
                        text = "+ 새 Jetson 등록",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToJetsonSpaceRegister
                    )
                }
            }
        }
    }

    if (showNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSheet = false },
            sheetState = sheetState,
            containerColor = OnSafeColor.Card
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "알림 기록",
                    color = OnSafeColor.TextPrimary,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (alertHistory.isEmpty()) {
                    Text(
                        text = "최근 발생한 위험 알림이 없습니다.",
                        color = OnSafeColor.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                } else {
                    alertHistory.forEach { alert ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (alert.eventId != null) {
                                        showNotificationSheet = false
                                        onNavigateToReport(alert.eventId)
                                    }
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
                                        text = alert.message ?: "위험 알림이 발생했습니다.",
                                        color = Color.Red,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "카메라명: ${alert.cameraName ?: "알 수 없음"}",
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

@Composable
private fun EmptyJetsonCard(
    isScanning: Boolean,
    onRegisterClick: () -> Unit
) {
    OnSafeCard(modifier = Modifier.fillMaxWidth(), selected = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                tint = OnSafeColor.Blue,
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "등록된 Jetson이 없습니다.",
                    color = OnSafeColor.TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isScanning) {
                        "mDNS로 Jetson을 찾는 중입니다."
                    } else {
                        "Jetson을 먼저 등록해야 모니터링을 시작할 수 있습니다."
                    },
                    color = OnSafeColor.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        OnSafePrimaryButton(
            text = "Jetson 등록하기",
            modifier = Modifier.fillMaxWidth(),
            onClick = onRegisterClick
        )
    }
}

@Composable
private fun RegisteredJetsonCard(
    jetson: JetsonDevice,
    primary: Boolean,
    onClick: () -> Unit
) {
    OnSafeCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        selected = primary
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                tint = OnSafeColor.Blue,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = jetson.name,
                        color = OnSafeColor.TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (primary) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OnSafeSmallPill("주 현장")
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = jetson.spaceName ?: "공간 미지정",
                    color = OnSafeColor.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "${jetson.ipAddress}:${jetson.port}",
                    color = OnSafeColor.TextTertiary,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                OnSafeStatusDot(
                    color = if (jetson.status) OnSafeColor.Green else OnSafeColor.Gray,
                    text = if (jetson.status) "연결됨" else "오프라인"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniStat(value = jetson.sensorTotal.toString(), label = "센서")
                    MiniStat(value = jetson.cctvTotal.toString(), label = "CCTV")
                    MiniStat(value = jetson.workerTotal.toString(), label = "작업자")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "눌러서 현장으로 이동",
                    color = OnSafeColor.Blue,
                    style = MaterialTheme.typography.bodySmall
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
private fun MiniStat(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = OnSafeColor.TextPrimary,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            color = OnSafeColor.TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}