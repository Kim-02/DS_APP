package com.example.ds_safer.domain.model

import com.google.gson.annotations.SerializedName

// 파이썬의 'CameraCreate' 스키마와 100% 일치하는 요청 데이터
data class CctvRegisterRequest(
    @SerializedName("sensor_type")
    val sensorType: String = "CAM",

    @SerializedName("sen_name")
    val senName: String,

    @SerializedName("status")
    val status: String = "ready",

    @SerializedName("jetson_id")
    val jetsonId: Int,

    @SerializedName("ip_address")
    val ipAddress: String,

    // 🚨 파이썬 스키마에 없으므로 port는 과감하게 삭제했습니다!
    // 만약 필요하다면 ipAddress 문자열 안에 "192.168.0.x:554" 처럼 합쳐서 보내는 것을 추천합니다.

    @SerializedName("camera_id")
    val cameraId: String,

    @SerializedName("camera_pw")
    val cameraPw: String
)

// 파이썬 API의 리턴값
data class CctvResponse(
    @SerializedName("message")
    val message: String?,                    // 에러가 날 수도 있으니 null 허용(?) 처리

    @SerializedName("sen_id")
    val senId: Int? = null
)