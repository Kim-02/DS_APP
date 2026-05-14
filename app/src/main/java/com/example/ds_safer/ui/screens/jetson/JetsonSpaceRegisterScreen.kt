package com.example.ds_safer.ui.screens.jetson

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.domain.model.SpaceDto
import com.example.ds_safer.ui.screens.discovery.DiscoveryViewModel
import com.example.ds_safer.ui.theme.OnSafeCard
import com.example.ds_safer.ui.theme.OnSafeColor
import com.example.ds_safer.ui.theme.OnSafePrimaryButton
import com.example.ds_safer.ui.theme.OnSafeScreen
import com.example.ds_safer.ui.theme.OnSafeSectionTitle
import com.example.ds_safer.ui.theme.OnSafeStatusDot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JetsonSpaceRegisterScreen(
    discoveryViewModel: DiscoveryViewModel,
    viewModel: JetsonSpaceRegisterViewModel,
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val discoveredJetsons by discoveryViewModel.discoveredJetsons.collectAsState()
    val isScanning by discoveryViewModel.isScanning.collectAsState()

    val spaces by viewModel.spaces.collectAsState()
    val selectedJetson by viewModel.selectedJetson.collectAsState()
    val selectedSpace by viewModel.selectedSpace.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    OnSafeScreen {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = OnSafeColor.TextPrimary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "현장 연결 설정",
                    color = OnSafeColor.TextPrimary,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "mDNS로 발견된 Jetson을 선택하고 작업장을 연결하세요.",
                    color = OnSafeColor.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                tint = OnSafeColor.Blue,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OnSafeSectionTitle("발견된 Jetson", modifier = Modifier.weight(1f))
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = OnSafeColor.Blue
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (discoveredJetsons.isEmpty()) {
                item {
                    OnSafeCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "발견된 Jetson이 없습니다.",
                            color = OnSafeColor.TextPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Jetson 서버가 실행 중인지, 같은 네트워크인지 확인해주세요.",
                            color = OnSafeColor.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                items(discoveredJetsons) { device ->
                    JetsonCandidateCard(
                        device = device,
                        selected = selectedJetson?.ipAddress == device.ipAddress &&
                                selectedJetson?.port == device.port,
                        onClick = {
                            viewModel.selectJetson(device)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                OnSafeSectionTitle("작업장 선택")
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (selectedJetson == null) {
                item {
                    Text(
                        text = "먼저 Jetson을 선택하면 작업장 목록을 불러옵니다.",
                        color = OnSafeColor.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else if (spaces.isEmpty() && isLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = OnSafeColor.Blue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "작업장 목록을 불러오는 중입니다.",
                            color = OnSafeColor.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                items(spaces) { space ->
                    SpaceCard(
                        space = space,
                        selected = selectedSpace?.spaceId == space.spaceId,
                        onClick = {
                            viewModel.selectSpace(space)
                        }
                    )
                }
            }

            item {
                successMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = OnSafeColor.Green,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = OnSafeColor.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                OnSafePrimaryButton(
                    text = if (isLoading) "등록 중..." else "Jetson 등록하기",
                    enabled = selectedJetson != null && selectedSpace != null && !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.registerSelectedJetson(
                            onSuccess = onRegisterSuccess
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun JetsonCandidateCard(
    device: JetsonDevice,
    selected: Boolean,
    onClick: () -> Unit
) {
    OnSafeCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        selected = selected
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = if (selected) OnSafeColor.Blue else OnSafeColor.TextSecondary,
                modifier = Modifier.size(38.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    color = OnSafeColor.TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${device.ipAddress}:${device.port}",
                    color = OnSafeColor.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                OnSafeStatusDot(
                    color = if (device.status) OnSafeColor.Green else OnSafeColor.Gray,
                    text = if (device.status) "온라인" else "발견됨"
                )
            }

            SelectCircle(selected = selected)
        }
    }
}

@Composable
private fun SpaceCard(
    space: SpaceDto,
    selected: Boolean,
    onClick: () -> Unit
) {
    OnSafeCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        selected = selected
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (selected) OnSafeColor.Blue else OnSafeColor.TextSecondary,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = space.spaceName,
                    color = OnSafeColor.TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "space_id: ${space.spaceId}",
                    color = OnSafeColor.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            SelectCircle(selected = selected)
        }
    }
}

@Composable
private fun SelectCircle(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                color = if (selected) OnSafeColor.Blue else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(Color.Transparent, CircleShape)
            )
        }
    }
}