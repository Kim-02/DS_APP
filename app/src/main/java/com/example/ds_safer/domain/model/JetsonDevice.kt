package com.example.ds_safer.domain.model

data class JetsonDevice(
    val name: String,
    val ipAddress: String,
    val port: Int,
    val status: Boolean = false,
    val jetsonId: Int? = null,
    val spaceId: Int? = null,
    val spaceName: String? = null,
    val isRegistered: Boolean = false,
    val sensorTotal: Int = 0,
    val cctvTotal: Int = 0,
    val workerTotal: Int = 0
)