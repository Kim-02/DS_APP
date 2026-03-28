package com.example.ds_safer.domain.model

data class SensorInfo(
    val id: String,      // 센서 고유 ID (예: "watch_01")
    val type: String,    // 센서 종류 (예: "HeartRate", "Gps")
    val status: String,  // 현재 상태 (예: "ready")
)