package com.example.ds_safer.util.nsd

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.ds_safer.domain.model.JetsonDevice
import java.util.UUID

class NsdHelper(context: Context) {
    private val TAG = "NsdHelper"
    private val SERVICE_TYPE = "_jetsonhub._tcp."

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var isDiscoveryActive = false

    interface NsdDiscoveryListener {
        fun onDeviceFound(device: JetsonDevice)
        fun onDiscoveryStarted()
        fun onDiscoveryStopped()
    }

    private var listener: NsdDiscoveryListener? = null

    // ★ 수정 1: val에서 var로 변경하고, 일단 null로 비워둡니다.
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    fun startDiscovery(listener: NsdDiscoveryListener) {
        this.listener = listener

        if (isDiscoveryActive) {
            Log.d(TAG, "Discovery already active. Skipping start.")
            return
        }

        // ★ 수정 2: 탐색을 시작할 때마다 "새로운" 리스너를 찍어냅니다! (연속 클릭 시 앱 튕김 방지)
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started")
                isDiscoveryActive = true
                this@NsdHelper.listener?.onDiscoveryStarted()
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${serviceInfo.serviceName}")

                nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.e(TAG, "Resolve failed: Error code:$errorCode")
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        Log.d(TAG, "Resolve Succeeded. ${serviceInfo.host} : ${serviceInfo.port}")

                        val hostAddress = serviceInfo.host?.hostAddress
                        if (!hostAddress.isNullOrBlank()) {
                            val device = JetsonDevice(
                                id = UUID.randomUUID().toString(),
                                name = serviceInfo.serviceName,
                                ipAddress = hostAddress,
                                port = serviceInfo.port
                            )
                            this@NsdHelper.listener?.onDeviceFound(device)
                        } else {
                            Log.e(TAG, "Resolve Succeeded but IP is null or empty.")
                        }
                    }
                })
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.e(TAG, "service lost: $serviceInfo")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(TAG, "Discovery stopped: $serviceType")
                isDiscoveryActive = false
                this@NsdHelper.listener?.onDiscoveryStopped()
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code:$errorCode")
                isDiscoveryActive = false
                try { nsdManager.stopServiceDiscovery(this) } catch (e: Exception) { }
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Stop failed: Error code:$errorCode")
                isDiscoveryActive = false
                try { nsdManager.stopServiceDiscovery(this) } catch (e: Exception) { }
            }
        }

        try {
            // 방금 만든 따끈따끈한 리스너를 넣고 시작!
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "discoverServices failed", e)
            isDiscoveryActive = false
        }
    }

    fun stopDiscovery() {
        if (!isDiscoveryActive || discoveryListener == null) {
            Log.d(TAG, "No active discovery to stop.")
            this.listener = null
            return
        }

        try {
            // ★ 수정 3: null이 아닐 때만 안전하게 종료를 요청합니다.
            discoveryListener?.let {
                nsdManager.stopServiceDiscovery(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "stopServiceDiscovery failed", e)
        } finally {
            isDiscoveryActive = false
            this.listener = null
            this.discoveryListener = null // ★ 다음 스캔을 위해 리스너를 비워줍니다.
        }
    }
}