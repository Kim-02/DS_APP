package com.example.ds_safer.ui.screens.monitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MonitoringActivity : ComponentActivity() {

    // 🌟 컴포즈용 상태 관리 (데이터가 들어오면 화면이 알아서 갱신됨)
    private val vitalLogs = mutableStateListOf<String>("> 🟢 Vital 모니터링 대기 중...")
    private val thLogs = mutableStateListOf<String>("> 🔵 온습도 모니터링 대기 중...")

    private var vitalWebSocket: WebSocket? = null
    private var thWebSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 이전 화면에서 넘겨준 IP 받기
        val jetsonIp = intent.getStringExtra("JETSON_IP") ?: "192.168.0.66"

        // 웹소켓 연결 시작
        startVitalWebSocket(jetsonIp)
        startThWebSocket(jetsonIp)

        // 🌟 여기가 컴포즈 화면을 그리는 곳! (XML 대신 사용)
        setContent {
            MaterialTheme {
                MonitoringScreen(vitalLogs, thLogs)
            }
        }
    }

    private fun startVitalWebSocket(ip: String) {
        val request = Request.Builder().url("ws://$ip:8000/ws/vital").build()
        vitalWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                runOnUiThread {
                    try {
                        val json = JSONObject(text)
                        val hr = json.optDouble("hr", 0.0)
                        vitalLogs.add("> [수신] 심박수: $hr bpm")
                    } catch (e: Exception) {
                        vitalLogs.add("> $text")
                    }
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                runOnUiThread { vitalLogs.add("❌ 워치 통신 에러: ${t.message}") }
            }
        })
    }

    private fun startThWebSocket(ip: String) {
        val request = Request.Builder().url("ws://$ip:8000/ws/th").build()
        thWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                runOnUiThread {
                    try {
                        val json = JSONObject(text)
                        val temp = json.optDouble("temp", 0.0)
                        val humid = json.optDouble("humid", 0.0)
                        thLogs.add("> [수신] 온도: $temp°C, 습도: $humid%")
                    } catch (e: Exception) {
                        thLogs.add("> $text")
                    }
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                runOnUiThread { thLogs.add("❌ 온습도 통신 에러: ${t.message}") }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        vitalWebSocket?.close(1000, "User exited")
        thWebSocket?.close(1000, "User exited")
        client.dispatcher.executorService.shutdown()
    }
}

// ==============================================================
// 🎨 아래부터는 컴포즈 UI 그리는 코드 (activity_monitoring.xml 대체)
// ==============================================================

@Composable
fun MonitoringScreen(vitalLogs: List<String>, thLogs: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // 연회색 배경
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        // 상단: 워치 데이터
        Text("⌚ 스마트워치 실시간 데이터", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Spacer(modifier = Modifier.height(8.dp))
        TerminalBox(modifier = Modifier.weight(1f), logs = vitalLogs)

        Spacer(modifier = Modifier.height(20.dp))

        // 하단: 온습도 데이터
        Text("🌡️ 온습도 실시간 데이터", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Spacer(modifier = Modifier.height(8.dp))
        TerminalBox(modifier = Modifier.weight(1f), logs = thLogs)
    }
}

@Composable
fun TerminalBox(modifier: Modifier = Modifier, logs: List<String>) {
    val listState = rememberLazyListState()

    // 🌟 새로운 데이터가 리스트에 추가될 때마다 맨 아래로 자동 스크롤!
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(logs) { log ->
                Text(text = log, color = Color(0xFF333333), fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}