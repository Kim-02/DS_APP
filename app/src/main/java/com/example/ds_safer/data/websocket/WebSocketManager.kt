package com.example.ds_safer.data.websocket

import android.util.Log
import com.example.ds_safer.data.repository.AlertRepository
import com.example.ds_safer.domain.model.HazardAlert
import com.google.gson.Gson
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import org.json.JSONObject

object WebSocketManager {
    private const val TAG = "WS_APP"

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private var currentConnectedIp: String? = null
    private var currentConnectedPort: Int = -1

    private val _alertFlow = MutableSharedFlow<HazardAlert>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val alertFlow = _alertFlow.asSharedFlow()

    /**
     * WebSocket 연결.
     * @param ipAddress Jetson 서버 IP
     * @param port      서버 포트 (API 포트와 동일, 기본 8000)
     */
    fun connect(ipAddress: String, port: Int = 8000) {
        if (currentConnectedIp == ipAddress && currentConnectedPort == port && webSocket != null) {
            Log.d(TAG, "이미 [$ipAddress:$port]에 연결되어 있습니다. 중복 연결 무시.")
            return
        }
        disconnect()

        currentConnectedIp = ipAddress
        currentConnectedPort = port
        val url = "ws://$ipAddress:$port/ws/alerts"
        Log.d(TAG, "connecting url=$url")

        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "connected url=$url")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "onMessage raw=$text")
                try {
                    val obj = JSONObject(text)
                    val type = obj.optString("type")
                    Log.d(TAG, "onMessage type=$type")

                    if (type == "hazard_alert") {
                        val alert = Gson().fromJson(text, HazardAlert::class.java)
                        Log.d(TAG, "hazard_alert parsed event_id=${alert.eventId} space_id=${alert.spaceId} level=${alert.level}")

                        // AlertRepository 즉시 반영 (StateFlow → UI 자동 갱신)
                        AlertRepository.prependFromHazardAlert(alert)

                        // SharedFlow emit (화면별 notification 트리거)
                        val emitted = _alertFlow.tryEmit(alert)
                        Log.d(TAG, "alertFlow emit event_id=${alert.eventId} result=$emitted")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "onMessage parse error: ${e.message} raw=$text")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "failure url=$url error=${t.message} response=${response?.code}")
                currentConnectedIp = null
                currentConnectedPort = -1
                this@WebSocketManager.webSocket = null
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "closed url=$url code=$code reason=$reason")
                currentConnectedIp = null
                currentConnectedPort = -1
                this@WebSocketManager.webSocket = null
            }
        })
    }

    fun disconnect() {
        webSocket?.cancel()
        webSocket = null
        currentConnectedIp = null
        currentConnectedPort = -1
        Log.d(TAG, "disconnected")
    }

    fun isConnected(): Boolean = webSocket != null && currentConnectedIp != null
}
