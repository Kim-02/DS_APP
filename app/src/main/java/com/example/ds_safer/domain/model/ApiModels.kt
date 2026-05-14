package com.example.ds_safer.domain.model

import com.google.gson.annotations.SerializedName

data class JetsonInfoResponse(
    @SerializedName("jetson_id") val jetsonId: Int
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
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("space_id") val spaceId: Int? = null,
    @SerializedName("space_name") val spaceName: String? = null,

    @SerializedName("placed") val placed: Int? = null,
    @SerializedName("x_ratio") val xRatio: Float? = null,
    @SerializedName("y_ratio") val yRatio: Float? = null
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

// ==========================================
// 평면도 응답
// ==========================================
data class FloorMapResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: FloorMapInfo
)

data class FloorMapInfo(
    @SerializedName("map_id") val mapId: Int,
    @SerializedName("jetson_id") val jetsonId: Int? = null,
    @SerializedName("space_id") val spaceId: Int? = null,
    @SerializedName("map_name") val mapName: String,
    @SerializedName("image_base64") val imageBase64: String,
    @SerializedName("image_mime_type") val imageMimeType: String?,
    @SerializedName("image_width") val imageWidth: Int?,
    @SerializedName("image_height") val imageHeight: Int?
)

data class SensorPositionListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<SensorMapPosition>
)

data class SensorMapPosition(
    @SerializedName("position_id") val positionId: Int?,
    @SerializedName("map_id") val mapId: Int,
    @SerializedName("sensor_id") val sensorId: String,
    @SerializedName("x_ratio") val xRatio: Float,
    @SerializedName("y_ratio") val yRatio: Float,
    @SerializedName("sen_name") val senName: String?,
    @SerializedName("sensor_type") val sensorType: String?,
    @SerializedName("sen_locate") val senLocate: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("is_online") val isOnline: Int?
)

data class SaveSensorPositionRequest(
    @SerializedName("map_id") val mapId: Int,
    @SerializedName("sensor_id") val sensorId: String,
    @SerializedName("x_ratio") val xRatio: Float,
    @SerializedName("y_ratio") val yRatio: Float
)

data class LatestTempHumidityResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: LatestTempHumidityData?
)

data class LatestTempHumidityData(
    @SerializedName("temp") val temp: Float?,
    @SerializedName("humid") val humid: Float?,
    @SerializedName("time") val time: String?
)

// ==========================================
// 실제 MariaDB worker 테이블 기준 작업자 응답
// ==========================================
data class WorkerDbResponse(
    @SerializedName("dept_id") val deptId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("is_manager") val isManager: Int,
    @SerializedName("sen_id") val senId: Int? = null,
    @SerializedName("sensor_id") val sensorId: String? = null,
    @SerializedName("sensor_type") val sensorType: String? = null,
    @SerializedName("sensor_name") val sensorName: String? = null
)

// ==========================================
// 워치-작업자 매핑 요청
// POST /workers/{dept_id}/assign-heart-band
// ==========================================
data class AssignHeartBandRequest(
    @SerializedName("sensor_id") val sensorId: String,
    @SerializedName("jetson_id") val jetsonId: Int? = null,
    @SerializedName("interval_ms") val intervalMs: Int = 5000
)

data class AssignHeartBandResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AssignedHeartBandData? = null
)

data class AssignedHeartBandData(
    @SerializedName("sen_id") val senId: Int,
    @SerializedName("sensor_id") val sensorId: String,
    @SerializedName("sensor_type") val sensorType: String,
    @SerializedName("dept_id") val deptId: Int,
    @SerializedName("worker_name") val workerName: String,
    @SerializedName("mqtt_base") val mqttBase: String? = null,
    @SerializedName("mqtt_topic") val mqttTopic: String? = null
)

data class UnassignSensorResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

// ==========================================
// CCTV 모듈 기준 모델
// 기준 서버 모듈:
// - schemas.py
// - router.py
// - service.py
//
// 사용 API:
// POST /cctv/cameras/register
// GET  /cctv/cameras/
// POST /cctv/cameras/
// GET  /cctv/cameras/{sensor_id}
// PUT  /cctv/cameras/{sensor_id}
// DELETE /cctv/cameras/{sensor_id}
// ==========================================

// 앱 전용 CCTV 등록 요청
// 사용자는 IP/PW만 입력하고,
// cameraUsername은 "admin" 기본값 사용
// name은 앱에서 "CCTV-{IP}" 등으로 자동 생성
// spaceId는 현재 선택된 Jetson의 spaceId를 사용 (하드코딩 금지)
data class AppCameraRegisterRequest(
    @SerializedName("ip_address") val ipAddress: String,
    @SerializedName("camera_username") val cameraUsername: String = "admin",
    @SerializedName("camera_password") val cameraPassword: String,
    @SerializedName("name") val name: String,
    @SerializedName("space_id") val spaceId: Int?,
    @SerializedName("jetson_id") val jetsonId: Int? = null,
    @SerializedName("rtsp_path") val rtspPath: String? = null
)

// RTSP URL을 직접 알고 있을 때 사용하는 생성 요청
data class CameraCreateRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("name") val name: String,
    @SerializedName("space_id") val spaceId: Int,
    @SerializedName("rtsp_url") val rtspUrl: String
)

data class CameraUpdateRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("is_active") val isActive: Boolean? = null,
    @SerializedName("space_id") val spaceId: Int? = null,
    @SerializedName("rtsp_url") val rtspUrl: String? = null
)

data class CameraOutResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("name") val name: String,
    @SerializedName("space_id") val spaceId: Int,
    @SerializedName("space_name") val spaceName: String? = null,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("registered_at") val registeredAt: String,
    @SerializedName("camera") val camera: CameraDetailResponse?
)

data class CameraDetailResponse(
    @SerializedName("rtsp_url") val rtspUrl: String
)

data class FirePipelineStatusResponse(
    @SerializedName("camera_id") val cameraId: Int,
    @SerializedName("running") val running: Boolean,
    @SerializedName("latest_result") val latestResult: String?
)

data class SimplePipelineResponse(
    @SerializedName("status") val status: String,
    @SerializedName("camera_id") val cameraId: Int
)

// ==========================================
// Jetson-space 등록 모델
// ==========================================

data class SpaceDto(
    @SerializedName("space_id") val spaceId: Int,
    @SerializedName("space_name") val spaceName: String
)

data class SpaceListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<SpaceDto>
)

data class JetsonAppRegisterRequest(
    @SerializedName("jetson_wp") val jetsonWp: String,
    @SerializedName("jetson_loc") val jetsonLoc: String,
    @SerializedName("jetson_status") val jetsonStatus: Boolean = true,
    @SerializedName("ip_addr") val ipAddr: String,
    @SerializedName("port") val port: Int = 8080,
    @SerializedName("space_id") val spaceId: Int
)

data class JetsonAppRegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: JetsonOutDto? = null
)

data class JetsonOutDto(
    @SerializedName("jetson_id") val jetsonId: Int,
    @SerializedName("jetson_wp") val jetsonWp: String,
    @SerializedName("jetson_loc") val jetsonLoc: String,
    @SerializedName("jetson_status") val jetsonStatus: Boolean,
    @SerializedName("ip_addr") val ipAddr: String,
    @SerializedName("port") val port: Int,
    @SerializedName("space_id") val spaceId: Int? = null,
    @SerializedName("space_name") val spaceName: String? = null
)

data class JetsonUnregisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)