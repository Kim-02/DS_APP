package com.example.ds_safer.data.api

import com.example.ds_safer.domain.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    // ==========================================
    // CCTV 모듈 API
    // 기준 Python router:
    // prefix = /cctv/cameras
    // ==========================================

    // CCTV 목록 조회
    @GET("/api/v1/cctv/cameras/")
    suspend fun getCameras(
        @Query("process_id") processId: Int? = null
    ): List<CameraOutResponse>

    // CCTV 직접 생성
    // 이미 RTSP URL을 알고 있을 때 사용
    @POST("cctv/cameras/")
    suspend fun createCamera(
        @Body request: CameraCreateRequest
    ): CameraOutResponse

    // CCTV 앱용 등록
    // 앱에서 IP/PW 입력
    // camera_username은 AppCameraRegisterRequest에서 "admin" 기본값 사용
    // 서버에서 RTSP URL 자동 생성
    @POST("/api/v1/cctv/cameras/register")
    suspend fun registerCctv(
        @Body request: AppCameraRegisterRequest
    ): CameraOutResponse

    // CCTV 상세 조회
    // Python router에서는 sensor_id 기준으로 get_camera 호출
    @GET("/api/v1/cctv/cameras/{sensorId}")
    suspend fun getCamera(
        @Path("sensorId") sensorId: Int
    ): CameraOutResponse

    // CCTV 수정
    @PUT("/api/v1/cctv/cameras/{sensorId}")
    suspend fun updateCamera(
        @Path("sensorId") sensorId: Int,
        @Body request: CameraUpdateRequest
    ): CameraOutResponse

    // CCTV 삭제
    @DELETE("/api/v1/cctv/cameras/{sensorId}")
    suspend fun deleteCamera(
        @Path("sensorId") sensorId: Int
    ): Response<Unit>

    // Fire pipeline 상태 조회
    // 주의:
    // 현재 Python router는 path 이름을 camera_id로 쓰고 있으나,
    // start/stop 내부에서는 service.get_camera(db, camera_id)를 호출합니다.
    // 따라서 앱에서는 일단 CameraOutResponse.id, 즉 sensor id 기준으로 넘기는 구조로 맞춥니다.
    @GET("/api/v1/cctv/cameras/{sensorId}/fire-pipeline")
    suspend fun getFirePipelineStatus(
        @Path("sensorId") sensorId: Int
    ): FirePipelineStatusResponse

    // Fire pipeline 시작
    @POST("/api/v1/cctv/cameras/{sensorId}/fire-pipeline/start")
    suspend fun startFirePipeline(
        @Path("sensorId") sensorId: Int
    ): SimplePipelineResponse

    // Fire pipeline 중단
    @POST("/api/v1/cctv/cameras/{sensorId}/fire-pipeline/stop")
    suspend fun stopFirePipeline(
        @Path("sensorId") sensorId: Int
    ): SimplePipelineResponse

    // ==========================================
    // 작업자 / 이벤트 / 지도 / 센서 API
    // ==========================================

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

    @GET("api/maps/{jetsonId}/available-sensors")
    suspend fun getAvailableTempSensorsForMap(
        @Path("jetsonId") jetsonId: Int,
        @Query("map_id") mapId: Int? = null
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