package com.example.ds_safer.data.api

import com.example.ds_safer.domain.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class JetsonRegisterRequest(
    val dept_id: Int,
    val app_id: String
)

data class JetsonRegisterResponse(
    val jetson_id: String,
    val register_status: String,
    val api_base_url: String,
    val ws_url: String
)

interface JetsonApiService {

    // 1. 젯슨 헬스 체크
    @GET("/")
    suspend fun checkHealth(): Response<ResponseBody>

    // 서버에 실제로 없으면 제거하거나 서버 쪽에 추가 필요
    @GET("api/jetson")
    suspend fun getJetsonInfo(): JetsonInfoResponse

    @POST("api/jetson/register")
    suspend fun registerJetson(
        @Body request: JetsonRegisterRequest
    ): Response<JetsonRegisterResponse>

    // 발견된 센서 목록
    @GET("api/sensors/discovered")
    suspend fun getDiscoverSensors(): SensorDiscoveryResponse

    // 등록된 센서 목록
    @GET("api/sensors")
    suspend fun getRegisteredSensors(): SensorListResponse

    // 센서 등록
    @POST("api/sensors/register")
    suspend fun registerSensor(
        @Body request: SensorRegisterRequest
    ): SimpleResponse

    // 센서 등록 해제
    @POST("api/sensors/unregister")
    suspend fun unregisterSensor(
        @Body request: SensorUnregisterRequest
    ): SimpleResponse

    // CCTV 카메라 등록
    @POST("api/cameras/register")
    suspend fun registerCctv(
        @Body request: CameraCreate
    ): CameraRegisterResponse

    // 등록된 CCTV 목록
    @GET("api/cameras")
    suspend fun getRegisteredCameras(): CctvListResponse

    @GET("api/worker")
    suspend fun getWorkerName(
        @Query("worker_id") workerId: String
    ): Response<WorkerNameResponse>

    @POST("api/event/measures")
    suspend fun postEventMeasures(
        @Body request: EventMeasuresReq
    ): Response<SimpleResponse>

    @GET("api/maps/{jetsonId}")
    suspend fun getFloorMap(
        @Path("jetsonId") jetsonId: Int
    ): FloorMapResponse

    @GET("api/maps/{mapId}/sensors")
    suspend fun getMapSensorPositions(
        @Path("mapId") mapId: Int
    ): SensorPositionListResponse

    @GET("api/maps/{jetsonId}/available-temp-sensors")
    suspend fun getAvailableTempSensorsForMap(
        @Path("jetsonId") jetsonId: Int
    ): SensorListResponse

    @POST("api/maps/sensors/position")
    suspend fun saveSensorPosition(
        @Body request: SaveSensorPositionRequest
    ): SimpleResponse

    @GET("api/maps/sensors/{sensorId}/latest")
    suspend fun getLatestTempSensorValue(
        @Path("sensorId") sensorId: String
    ): LatestTempHumidityResponse

    // 실제 MariaDB worker 목록 조회
    @GET("api/v1/workers/db")
    suspend fun getDbWorkers(
        @Query("is_manager") isManager: Int = 0
    ): List<WorkerDbResponse>

    // 사번으로 실제 MariaDB worker 조회
    @GET("api/v1/workers/db/{deptId}")
    suspend fun getDbWorker(
        @Path("deptId") deptId: Int
    ): WorkerDbResponse

    // mDNS로 발견된 heart_band 워치를 작업자에게 등록 및 매핑
    @POST("api/v1/workers/{deptId}/assign-heart-band")
    suspend fun assignHeartBandToWorker(
        @Path("deptId") deptId: Int,
        @Body request: AssignHeartBandRequest
    ): AssignHeartBandResponse

    // 작업자와 센서 매핑 해제
    @POST("api/v1/workers/{deptId}/unassign-sensor")
    suspend fun unassignWorkerSensor(
        @Path("deptId") deptId: Int
    ): UnassignSensorResponse

}

