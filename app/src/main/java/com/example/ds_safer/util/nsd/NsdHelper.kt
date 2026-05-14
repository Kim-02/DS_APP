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
    private var isDiscoveryActive = false

    interface NsdDiscoveryListener {
        fun onDeviceFound(device: JetsonDevice)
        fun onDiscoveryStarted()
        fun onDiscoveryStopped()
    }

    private var listener: NsdDiscoveryListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    fun startDiscovery(listener: NsdDiscoveryListener) {
        this.listener = listener

        if (isDiscoveryActive) {
            Log.d(TAG, "Discovery already active. Skipping start.")
            return
        }

        discoveryListener = object : NsdManager.DiscoveryListener {

            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started: $regType")
                isDiscoveryActive = true
                this@NsdHelper.listener?.onDiscoveryStarted()
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${serviceInfo.serviceName}")

                nsdManager.resolveService(
                    serviceInfo,
                    object : NsdManager.ResolveListener {

                        override fun onResolveFailed(
                            serviceInfo: NsdServiceInfo,
                            errorCode: Int
                        ) {
                            Log.e(TAG, "Resolve failed: service=${serviceInfo.serviceName}, errorCode=$errorCode")
                        }

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            Log.d(
                                TAG,
                                "Resolve succeeded: ${serviceInfo.serviceName}, ${serviceInfo.host}:${serviceInfo.port}"
                            )

                            val hostAddress = serviceInfo.host?.hostAddress

                            if (hostAddress.isNullOrBlank()) {
                                Log.e(TAG, "Resolve succeeded but IP is null or empty.")
                                return
                            }

                            val device = JetsonDevice(
                                name = serviceInfo.serviceName,
                                ipAddress = hostAddress,
                                port = serviceInfo.port,
                                status = true,
                                jetsonId = null,
                                spaceId = null,
                                spaceName = null,
                                isRegistered = false
                            )

                            this@NsdHelper.listener?.onDeviceFound(device)
                        }
                    }
                )
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.e(TAG, "Service lost: $serviceInfo")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(TAG, "Discovery stopped: $serviceType")
                isDiscoveryActive = false
                this@NsdHelper.listener?.onDiscoveryStopped()
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Start discovery failed: serviceType=$serviceType, errorCode=$errorCode")
                isDiscoveryActive = false

                try {
                    nsdManager.stopServiceDiscovery(this)
                } catch (_: Exception) {
                }

                this@NsdHelper.listener?.onDiscoveryStopped()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Stop discovery failed: serviceType=$serviceType, errorCode=$errorCode")
                isDiscoveryActive = false

                try {
                    nsdManager.stopServiceDiscovery(this)
                } catch (_: Exception) {
                }

                this@NsdHelper.listener?.onDiscoveryStopped()
            }
        }

        try {
            nsdManager.discoverServices(
                SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener
            )
        } catch (e: Exception) {
            Log.e(TAG, "discoverServices failed", e)
            isDiscoveryActive = false
            this.listener?.onDiscoveryStopped()
        }
    }

    fun stopDiscovery() {
        if (!isDiscoveryActive || discoveryListener == null) {
            Log.d(TAG, "No active discovery to stop.")
            listener = null
            return
        }

        try {
            discoveryListener?.let {
                nsdManager.stopServiceDiscovery(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "stopServiceDiscovery failed", e)
        } finally {
            isDiscoveryActive = false
            listener = null
            discoveryListener = null
        }
    }
}