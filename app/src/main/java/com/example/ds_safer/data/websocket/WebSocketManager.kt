package com.example.ds_safer.data.websocket

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ds_safer.domain.model.HazardAlert
import com.google.gson.Gson
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*

object WebSocketManager {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    // 🌟 추가: 현재 폰이 누구랑 통화 중인지 IP를 기억해둠
    private var currentConnectedIp: String? = null

    private val _alertFlow = MutableSharedFlow<HazardAlert>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val alertFlow = _alertFlow.asSharedFlow()

    fun connect(ipAddress: String) {
        // 🌟 방어 1: 이미 똑같은 기기(IP)랑 연결된 상태면 두 번 전화 걸지 않음!
        if (currentConnectedIp == ipAddress && webSocket != null) {
            Log.d("WS", "이미 [$ipAddress]에 연결되어 있습니다. 중복 연결 무시.")
            return
        }

        // 🌟 방어 2: 만약 다른 IP로 연결 중이었다면 확실하게 기존 전화를 끊어버림
        disconnect()

        currentConnectedIp = ipAddress
        val url = "ws://$ipAddress:8000/ws/alerts"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WS", "✅ 웹소켓 연결 성공! ($ipAddress)")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WS", "📩 메시지 수신: $text")
                try {
                    val alert = Gson().fromJson(text, HazardAlert::class.java)
                    _alertFlow.tryEmit(alert)
                } catch (e: Exception) {
                    Log.e("WS", "JSON 파싱 에러: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WS", "❌ 웹소켓 에러 발생: ${t.message}")
                currentConnectedIp = null // 에러로 끊기면 기억 삭제
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WS", "🔌 웹소켓 정상 종료됨")
                currentConnectedIp = null // 정상 종료돼도 기억 삭제
            }
        })
    }

    fun disconnect() {
        // 🌟 핵심 수정: close()는 너무 느림. cancel()로 리스너 스레드 즉각 강제 종료!
        webSocket?.cancel()
        webSocket = null
        currentConnectedIp = null
        Log.d("WS", "🚫 웹소켓 연결 강제 해제 완료")
    }

    fun sendSystemPushNotification(context: Context, alert: HazardAlert) {
        val channelId = "hazard_alert_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "위험 감지", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 위험 감지 알림")
            .setContentText(alert.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(alert.eventId.toInt(), notification)
    }
}