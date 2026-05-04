package com.example.ds_safer.domain.model

import com.google.gson.annotations.SerializedName

data class JetsonInfoResponse(
    @SerializedName("jetson_id") val jetsonId: Int
)

// ==========================================
// 카메라 등록 요청
// ==========================================
data class CameraCreate(
    @SerializedName("ip_address") val ipAddress: String,
    @SerializedName("camera_id") val cameraId: String,
    @SerializedName("camera_pw") val cameraPw: String
)

// ==========================================
// 발견 센서 목록 응답
// 서버:
// {
//   "status": "success",
//   "data": [ ... ]
// }
// ==========================================
data class SensorDiscoveryResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<DiscoveredSensor>
)

data class DiscoveredSensor(
    @SerializedName("sensor_id") val sensorId: String,
    @SerializedName("sensor_type") val sensorType: String,
    @SerializedName("sen_name") val senName: String,
    @SerializedName("sen_locate") val senLocate: String,
    @SerializedName("mqtt_topic") val mqttTopic: String,
    @SerializedName("model") val model: String? = null,
    @SerializedName("mdns_hostname") val mdnsHostname: String? = null,
    @SerializedName("ip_addr") val ipAddr: String? = null,
    @SerializedName("is_online") val isOnline: Boolean? = null,
    @SerializedName("last_seen_at") val lastSeenAt: String? = null
)

data class CameraRegisterResponse(
    @SerializedName("message") val message: String
)

// ==========================================
// 등록된 센서 목록 응답
// ==========================================
data class SensorListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<RegisteredSensor>
)

data class RegisteredSensor(
    @SerializedName("sen_id") val senId: Int? = null,
    @SerializedName("sensor_id") val sensorId: String,
    @SerializedName("jetson_id") val jetsonId: Int? = null,
    @SerializedName("sensor_type") val sensorType: String?,
    @SerializedName("sen_name") val senName: String?,
    @SerializedName("sen_locate") val senLocate: String?,
    @SerializedName("model") val model: String? = null,
    @SerializedName("mqtt_topic") val mqttTopic: String? = null,
    @SerializedName("mdns_hostname") val mdnsHostname: String? = null,
    @SerializedName("ip_addr") val ipAddr: String? = null,
    @SerializedName("is_online") val isOnline: Int? = null,
    @SerializedName("last_seen_at") val lastSeenAt: String? = null,
    @SerializedName("registered_at") val registeredAt: String? = null,
    @SerializedName("register_date") val registerDate: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

// ==========================================
// 센서 등록 요청
// ==========================================
data class SensorRegisterRequest(
    @SerializedName("jetson_id") val jetsonId: String,
    @SerializedName("selected_sensors") val selectedSensors: List<DiscoveredSensor>
)

// ==========================================
// 센서 등록 해제 요청
// ==========================================
data class SensorUnregisterRequest(
    @SerializedName("sensor_id") val sensorId: String
)

// ==========================================
// 공통 응답
// ==========================================
data class SimpleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

// ==========================================
// 작업자 이름 조회 응답
// ==========================================
data class WorkerNameResponse(
    @SerializedName("status") val status: String,
    @SerializedName("worker_name") val workerName: String
)

// ==========================================
// 사건 조치사항 요청
// ==========================================
data class EventMeasuresReq(
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("measures") val measures: String
)
