package com.example.ds_safer.domain.model

import com.google.gson.annotations.SerializedName

data class HazardAlert(
    @SerializedName("event_id") val eventId: Long? = null,
    @SerializedName("target_topic") val targetTopic: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("alert") val alert: Boolean? = null,

    @SerializedName("title") val title: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("level") val level: String? = null,
    @SerializedName("source") val source: String? = null,

    @SerializedName("space_id") val spaceId: Int? = null,
    @SerializedName("camera_sen_id") val cameraSenId: Int? = null,
    @SerializedName("sensor_id") val sensorId: String? = null,

    @SerializedName("color") val color: String? = null,
    @SerializedName("vibration") val vibration: Boolean? = null,

    @SerializedName("camera_name") val cameraName: String? = null,
    @SerializedName("camera_loc") val cameraLoc: String? = null,
    @SerializedName("ev_code_name") val evCodeName: String? = null,
    @SerializedName("event_time") val eventTime: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,

    @SerializedName("led") val led: Boolean? = null,
    @SerializedName("duration_ms") val durationMs: Long? = null,
    @SerializedName("reset_after_ms") val resetAfterMs: Long? = null
)
