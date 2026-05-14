package com.example.ds_safer.ui.screens.cctv

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ds_safer.domain.model.CameraOutResponse
import com.example.ds_safer.ui.theme.OnSafeCard
import com.example.ds_safer.ui.theme.OnSafeColor
import com.example.ds_safer.ui.theme.OnSafePrimaryButton
import com.example.ds_safer.ui.theme.OnSafeScreenBrush
import com.example.ds_safer.ui.theme.OnSafeSectionTitle
import com.example.ds_safer.ui.theme.OnSafeSmallPill
import com.example.ds_safer.ui.theme.OnSafeStatusDot

@Composable
fun CctvListScreen(
    viewModel: CctvListViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    onBackClick: () -> Unit
) {
    val cameraList by viewModel.cameraList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var deleteTarget by remember { mutableStateOf<CameraOutResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchCameras()
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = OnSafeColor.Card,
            titleContentColor = OnSafeColor.TextPrimary,
            textContentColor = OnSafeColor.TextSecondary,
            title = {
                Text("CCTV 삭제")
            },
            text = {
                Text("선택한 CCTV를 삭제하시겠습니까?\n서버에서 해당 CCTV 런타임도 함께 중지되어야 합니다.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = deleteTarget
                        deleteTarget = null
                        if (target != null) {
                            viewModel.deleteCamera(target.id)
                        }
                    }
                ) {
                    Text("삭제", color = OnSafeColor.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
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
                    text = "새 CCTV 등록하기",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
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
                            text = "등록된 CCTV 관리",
                            color = OnSafeColor.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "현재 공간 기준으로 등록된 CCTV입니다.",
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(onClick = { viewModel.fetchCameras() }) {
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
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = OnSafeColor.Blue
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CCTV 현황",
                                color = OnSafeColor.TextPrimary,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "전체 ${cameraList.size}대 · 활성 ${cameraList.count { it.isActive }}대",
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
                OnSafeSectionTitle("등록된 CCTV")
            }

            if (isLoading) {
                item {
                    LoadingBox("CCTV 목록을 불러오는 중입니다.")
                }
            } else if (cameraList.isEmpty()) {
                item {
                    EmptyGuideCard(
                        title = "등록된 CCTV가 없습니다.",
                        message = "새 CCTV 등록하기 버튼을 눌러 카메라를 등록해주세요."
                    )
                }
            } else {
                items(
                    items = cameraList,
                    key = { camera -> camera.id }
                ) { camera ->
                    CctvManageCard(
                        camera = camera,
                        onDeleteClick = {
                            deleteTarget = camera
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
private fun CctvManageCard(
    camera: CameraOutResponse,
    onDeleteClick: () -> Unit
) {
    OnSafeCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (camera.isActive) {
                            OnSafeColor.Blue.copy(alpha = 0.16f)
                        } else {
                            OnSafeColor.Gray.copy(alpha = 0.16f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "C",
                    color = if (camera.isActive) OnSafeColor.Blue else OnSafeColor.Gray,
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = camera.name,
                        color = OnSafeColor.TextPrimary,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    OnSafeStatusDot(
                        color = if (camera.isActive) OnSafeColor.Green else OnSafeColor.Gray,
                        text = if (camera.isActive) "활성" else "비활성"
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Sensor ID: ${camera.id} · Device ID: ${camera.deviceId}",
                    color = OnSafeColor.TextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Space ID: ${camera.spaceId ?: "미지정"}${camera.spaceName?.let { " · $it" } ?: ""}",
                    color = OnSafeColor.TextTertiary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                camera.camera?.rtspUrl?.let { rtspUrl ->
                    Text(
                        text = "RTSP: $rtspUrl",
                        color = OnSafeColor.TextTertiary,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "등록일: ${camera.registeredAt}",
                    color = OnSafeColor.TextTertiary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                modifier = Modifier.clickable {
                    onDeleteClick()
                },
                shape = RoundedCornerShape(12.dp),
                color = OnSafeColor.Red.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, OnSafeColor.Red.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = OnSafeColor.Red,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "삭제",
                        color = OnSafeColor.Red,
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