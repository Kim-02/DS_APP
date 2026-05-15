package com.example.ds_safer.ui.screens.report

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import AuthViewModel
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.EventMeasuresReq
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventReportScreen(
    eventId: Int,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val workerName by authViewModel.workerName.collectAsState()
    val deptId by authViewModel.currentDeptId.collectAsState()

    var measures by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("조치 사항 보고") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
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
            // 1. 이벤트 ID (어떤 사건인지)
            Text(text = "사건 번호: #$eventId", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // 2. 사번 (수정 불가 읽기 전용)
            OutlinedTextField(
                value = deptId,
                onValueChange = {},
                label = { Text("사번") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 3. 이름 (수정 불가 읽기 전용)
            OutlinedTextField(
                value = workerName,
                onValueChange = {},
                label = { Text("작업자 이름") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 4. 조치사항 입력창 (텍스트 박스)
            OutlinedTextField(
                value = measures,
                onValueChange = { measures = it },
                label = { Text("조치 사항 입력") },
                placeholder = { Text("예: 현장 확인 후 소화기 배치 완료 및 환기 실시") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // 화면 남은 공간 다 차지하게
                maxLines = 10
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 5. 제출 버튼
            Button(
                onClick = {
                    if (measures.isBlank()) {
                        Toast.makeText(context, "조치 사항을 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSubmitting = true
                    coroutineScope.launch {
                        try {
                            val jetson = JetsonRepository.selectedJetson.value
                            if (jetson == null) {
                                Toast.makeText(context, "연결된 Jetson이 없습니다.", Toast.LENGTH_SHORT).show()
                                isSubmitting = false
                                return@launch
                            }

                            val request = EventMeasuresReq(eventId = eventId, measures = measures)
                            val service = RetrofitClient.createService(
                                "http://${jetson.ipAddress}:${jetson.port}/"
                            )
                            val response = service.postEventMeasures(request)

                            if (response.isSuccessful && response.body()?.status == "success") {
                                Toast.makeText(context, "보고가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                onSubmitSuccess() // 완료 후 메인으로 돌아가기
                            } else {
                                Toast.makeText(context, "보고 실패: 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "서버 연결 오류", Toast.LENGTH_SHORT).show()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("제출하기")
                }
            }
        }
    }
}