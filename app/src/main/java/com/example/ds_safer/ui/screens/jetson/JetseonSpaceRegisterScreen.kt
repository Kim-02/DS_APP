package com.example.ds_safer.ui.screens.jetson

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ds_safer.domain.model.JetsonDevice
import com.example.ds_safer.domain.model.JetsonOutDto
import com.example.ds_safer.domain.model.SpaceDto
import com.example.ds_safer.ui.screens.discovery.DiscoveryViewModel

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
    val registeredJetsons by viewModel.registeredJetsons.collectAsState()
    val selectedJetson by viewModel.selectedJetson.collectAsState()
    val selectedSpace by viewModel.selectedSpace.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jetson 공간 등록") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("뒤로")
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
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "mDNS로 발견된 Jetson",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isScanning) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "주변 Jetson을 찾는 중입니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (discoveredJetsons.isEmpty()) {
                item {
                    EmptyGuideCard(
                        title = "발견된 Jetson이 없습니다.",
                        message = "Jetson 서버가 실행 중인지, 같은 네트워크에 연결되어 있는지 확인해주세요."
                    )
                }
            } else {
                items(discoveredJetsons) { device ->
                    DiscoveredJetsonCard(
                        device = device,
                        selected = selectedJetson == device,
                        onClick = {
                            viewModel.selectJetson(device)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "공간 선택",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                SpaceDropdown(
                    spaces = spaces,
                    selectedSpace = selectedSpace,
                    onSpaceSelected = {
                        viewModel.selectSpace(it)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.registerSelectedJetson(
                            onSuccess = onRegisterSuccess
                        )
                    },
                    enabled = selectedJetson != null && selectedSpace != null && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("선택한 Jetson 등록")
                }

                successMessage?.let {
                    MessageCard(
                        message = it,
                        isSuccess = true,
                        onClick = { viewModel.clearMessage() }
                    )
                }

                errorMessage?.let {
                    MessageCard(
                        message = it,
                        isSuccess = false,
                        onClick = { viewModel.clearMessage() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "선택한 Jetson 서버의 DB 등록 목록",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (registeredJetsons.isEmpty()) {
                item {
                    EmptyGuideCard(
                        title = "등록된 Jetson이 없습니다.",
                        message = "Jetson을 선택하면 해당 서버 DB의 등록 목록을 조회합니다."
                    )
                }
            } else {
                items(registeredJetsons) { jetson ->
                    RegisteredJetsonCard(
                        jetson = jetson,
                        onUnregisterClick = {
                            viewModel.deleteJetson(jetson.jetsonId) {
                                // 삭제 성공 후 메인 화면 Jetson 목록도 갱신
                                discoveryViewModel.loadRegisteredJetsonsFromDiscoveredServers()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoveredJetsonCard(
    device: JetsonDevice,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }
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
                    text = "${device.ipAddress}:${device.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "선택됨",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "선택",
                    tint = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpaceDropdown(
    spaces: List<SpaceDto>,
    selectedSpace: SpaceDto?,
    onSpaceSelected: (SpaceDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = selectedSpace?.spaceName ?: "공간을 선택하세요",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "공간 선택"
                )
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            spaces.forEach { space ->
                DropdownMenuItem(
                    text = {
                        Text("${space.spaceName} (${space.spaceId})")
                    },
                    onClick = {
                        onSpaceSelected(space)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RegisteredJetsonCard(
    jetson: JetsonOutDto,
    onUnregisterClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jetson.jetsonWp,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${jetson.ipAddr}:${jetson.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "등록 공간: ${jetson.spaceName ?: "미지정"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (jetson.jetsonStatus) "상태: 활성화" else "상태: 비활성화",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (jetson.jetsonStatus) Color(0xFF2E7D32) else Color.Gray
                )
            }

            TextButton(onClick = onUnregisterClick) {
                Text("해제", color = Color.Red)
            }
        }
    }
}

@Composable
private fun EmptyGuideCard(
    title: String,
    message: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    isSuccess: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isSuccess) Color(0xFF2E7D32) else Color.Red
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSuccess) Color(0xFF2E7D32) else Color.Red
            )
        }
    }
}