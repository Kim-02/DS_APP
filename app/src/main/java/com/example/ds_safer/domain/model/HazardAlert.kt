package com.example.ds_safer.domain.model

import com.google.gson.annotations.SerializedName

// 파이썬 api_payload 와 100% 일치하는 데이터 모델
data class HazardAlert(
    @SerializedName("event_id") val eventId: Long,
    @SerializedName("target_topic") val targetTopic: String,
    @SerializedName("type") val type: String,
    @SerializedName("alert") val alert: Boolean,

    @SerializedName("message") val message: String,
    @SerializedName("color") val color: String,
    @SerializedName("vibration") val vibration: Boolean,

    @SerializedName("camera_name") val cameraName: String,
    @SerializedName("camera_loc") val cameraLoc: String,
    @SerializedName("ev_code_name") val evCodeName: String,
    @SerializedName("event_time") val eventTime: String,

    // 👇 보내주신 코드에 맞춰 추가된 3가지 항목!
    @SerializedName("led") val led: Boolean,
    @SerializedName("duration_ms") val durationMs: Long,
    @SerializedName("reset_after_ms") val resetAfterMs: Long
)