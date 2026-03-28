package com.example.ds_safer.util.nsd

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.ds_safer.domain.model.JetsonDevice

class NsdHelper(context: Context) {
    private val TAG = "NsdHelper"
    private val SERVICE_TYPE = "_jetsonhub._tcp."

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    // [수정] 현재 탐색이 활성화 상태인지 추적하는 변수
    private var isDiscoveryActive = false

    interface NsdDiscoveryListener {
        fun onDeviceFound(device: JetsonDevice)
        fun onDiscoveryStarted()
        fun onDiscoveryStopped()
    }

    private var listener: NsdDiscoveryListener? = null

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
            isDiscoveryActive = true // 탐색 시작됨
            listener?.onDiscoveryStarted()
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Service found: ${serviceInfo.serviceName}")
            nsdManager.resolveService(serviceInfo, resolveListener)
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "service lost: $serviceInfo")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
            isDiscoveryActive = false // 탐색 멈춤
            listener?.onDiscoveryStopped()
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            isDiscoveryActive = false // 실패했으므로 활성화 아님
            try {
                nsdManager.stopServiceDiscovery(this)
            } catch (e: Exception) {
                // 이미 멈춘 경우 무시
            }
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Stop failed: Error code:$errorCode")
            isDiscoveryActive = false // 실패하더라도 리스너 해제 시도로 간주
            try {
                nsdManager.stopServiceDiscovery(this)
            } catch (e: Exception) {
                // 무시
            }
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "Resolve failed: Error code:$errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Resolve Succeeded. ${serviceInfo.host} : ${serviceInfo.port}")

            val device = JetsonDevice(
                name = serviceInfo.serviceName,
                ipAddress = serviceInfo.host.hostAddress ?: "",
                port = serviceInfo.port
            )
            listener?.onDeviceFound(device)
        }
    }

    // 외부에서 탐색 시작
    fun startDiscovery(listener: NsdDiscoveryListener) {
        this.listener = listener

        // [중요] 이미 탐색 중이라면 중복 시작 방지
        if (isDiscoveryActive) {
            Log.d(TAG, "Discovery already active. Skipping start.")
            return
        }

        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "discoverServices failed", e)
            isDiscoveryActive = false
        }
    }

    // 외부에서 탐색 중지
    fun stopDiscovery() {
        // [중요] 탐색 중일 때만 중지 시도 (에러 방지 핵심)
        if (!isDiscoveryActive) {
            Log.d(TAG, "No active discovery to stop.")
            this.listener = null
            return
        }

        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "stopServiceDiscovery failed", e)
        } finally {
            // 예외가 발생하더라도 상태는 초기화
            isDiscoveryActive = false
            this.listener = null
        }
    }
}