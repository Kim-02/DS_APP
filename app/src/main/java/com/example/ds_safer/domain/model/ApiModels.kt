package com.example.ds_safer.domain.model

import com.google.gson.annotations.SerializedName

data class JetsonInfoResponse(
    @SerializedName("jetson_id") val jetsonId: Int
    // 서버에서 오는거 뭔지 보고 더 추가하면 됨
)

// ==========================================
// 2. 센서 등록용 (Sensor)
// ==========================================
data class SensorCreate(
    @SerializedName("sensor_type") val sensorType: String,
    @SerializedName("sen_name") val senName: String,
    @SerializedName("sen_status") val status: Boolean,
    @SerializedName("jetson_id") val jetsonId: Int
)

// ==========================================
// 3. 카메라 등록용 (Camera)
// ==========================================
data class CameraCreate(
    @SerializedName("ip_address") val ipAddress: String,
    @SerializedName("camera_id") val cameraId: String,
    @SerializedName("camera_pw") val cameraPw: String
)

// ==========================================
// 4. 위험 정보 알림용 (Hazard Alert)
// ==========================================
data class HazardAlert(
    @SerializedName("sen_id") val senId: Int,
    @SerializedName("jetson_id") val jetsonId: Int,
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("detail") val detail: String
)

// API 응답 전체를 감싸는 껍데기 클래스
data class DiscoveredSensorsResponse(
    @SerializedName("jetson_id") val jetsonId: String,
    @SerializedName("discovered_sensors") val discoveredSensors: List<DiscoveredSensor>
)

// 리스트 안에 들어갈 센서 단일 아이템 (사진에 있는 JSON 키값과 완벽 일치)
data class DiscoveredSensor(
    @SerializedName("sen_name") val senName: String,
    @SerializedName("sensor_type") val sensorType: String,
    @SerializedName("mqtt_topic") val mqttTopic: String,
    @SerializedName("sen_locate") val senLocate: String
)

data class CameraRegisterResponse(
    @SerializedName("message") val message: String
)

data class SensorListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<RegisteredSensor>
)

data class RegisteredSensor(
    @SerializedName("sen_name") val senName: String?,
    @SerializedName("sensor_type") val sensorType: String?,
    @SerializedName("sen_locate") val senLocate: String?,
    @SerializedName("health") val health: Boolean? // 상태 값 (정상=true, 오류=false)
)

// 🆕 다중 센서 등록 요청(Request)용 데이터
data class SensorRegisterRequest(
    @SerializedName("jetson_id") val jetsonId: String,
    @SerializedName("selected_sensors") val selectedSensors: List<DiscoveredSensor>
)

// 🆕 다중 센서 등록 응답(Response)용 껍데기 (성공/실패 메시지)
data class SimpleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)