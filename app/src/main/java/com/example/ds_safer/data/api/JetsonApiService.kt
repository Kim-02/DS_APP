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

    // ==========================================
    // Health / Legacy Jetson API
    // ==========================================

    @GET("/")
    suspend fun checkHealth(): Response<ResponseBody>

    @GET("api/jetson")
    suspend fun getJetsonInfo(): JetsonInfoResponse

    /**
     * 구형 API.
     * mDNS Jetson 신규 등록용으로 사용하지 말 것.
     * 기존 앱 연동/호환용으로만 유지.
     */
    @POST("api/jetson/register")
    suspend fun registerJetson(
        @Body request: JetsonRegisterRequest
    ): Response<JetsonRegisterResponse>

    // ==========================================
    // Jetson-space 신규 등록 API
    // 서버:
    // GET  /api/v1/spaces
    // GET  /api/v1/jetsons
    // POST /api/v1/jetsons/register
    // POST /api/v1/jetsons/{jetson_id}/unregister
    // ==========================================

    @GET("/api/v1/spaces")
    suspend fun getSpaces(): SpaceListResponse

    @GET("/api/v1/jetsons")
    suspend fun getRegisteredJetsonsV1(): List<JetsonOutDto>

    @POST("/api/v1/jetsons/register")
    suspend fun registerJetsonFromMdns(
        @Body request: JetsonAppRegisterRequest
    ): JetsonAppRegisterResponse

    @POST("/api/v1/jetsons/{jetsonId}/unregister")
    suspend fun unregisterJetsonV1(
        @Path("jetsonId") jetsonId: Int
    ): JetsonUnregisterResponse

    /** Jetson 완전 삭제 (연결된 sensor + CCTV runtime 포함). */
    @DELETE("/api/v1/jetsons/{jetsonId}")
    suspend fun deleteJetsonV1(
        @Path("jetsonId") jetsonId: Int
    ): JetsonUnregisterResponse

    // ==========================================
    // Sensor API
    // ==========================================

    @GET("api/sensors/discovered")
    suspend fun getDiscoverSensors(): SensorDiscoveryResponse

    @GET("api/sensors")
    suspend fun getRegisteredSensors(): SensorListResponse

    @POST("api/sensors/register")
    suspend fun registerSensor(
        @Body request: SensorRegisterRequest
    ): SimpleResponse

    @POST("api/sensors/unregister")
    suspend fun unregisterSensor(
        @Body request: SensorUnregisterRequest
    ): SimpleResponse

    // ==========================================
    // CCTV API
    // ==========================================

    @GET("/api/v1/cctv/cameras/")
    suspend fun getCameras(
        @Query("space_id") spaceId: Int? = null
    ): List<CameraOutResponse>

    @POST("cctv/cameras/")
    suspend fun createCamera(
        @Body request: CameraCreateRequest
    ): CameraOutResponse

    @POST("/api/v1/cctv/cameras/register")
    suspend fun registerCctv(
        @Body request: AppCameraRegisterRequest
    ): CameraOutResponse

    @GET("/api/v1/cctv/cameras/{sensorId}")
    suspend fun getCamera(
        @Path("sensorId") sensorId: Int
    ): CameraOutResponse

    @PUT("/api/v1/cctv/cameras/{sensorId}")
    suspend fun updateCamera(
        @Path("sensorId") sensorId: Int,
        @Body request: CameraUpdateRequest
    ): CameraOutResponse

    @DELETE("/api/v1/cctv/cameras/{sensorId}")
    suspend fun deleteCamera(
        @Path("sensorId") sensorId: Int
    ): Response<Unit>

    @GET("/api/v1/cctv/cameras/{sensorId}/fire-pipeline")
    suspend fun getFirePipelineStatus(
        @Path("sensorId") sensorId: Int
    ): FirePipelineStatusResponse

    @POST("/api/v1/cctv/cameras/{sensorId}/fire-pipeline/start")
    suspend fun startFirePipeline(
        @Path("sensorId") sensorId: Int
    ): SimplePipelineResponse

    @POST("/api/v1/cctv/cameras/{sensorId}/fire-pipeline/stop")
    suspend fun stopFirePipeline(
        @Path("sensorId") sensorId: Int
    ): SimplePipelineResponse

    // ==========================================
    // Worker / Event / Map API
    // ==========================================

    @GET("api/worker")
    suspend fun getWorkerName(
        @Query("worker_id") workerId: String
    ): Response<WorkerNameResponse>

    @POST("api/event/measures")
    suspend fun postEventMeasures(
        @Body request: EventMeasuresReq
    ): Response<SimpleResponse>

    /** space_id 기준 평면도 조회 (권장). */
    @GET("api/maps/space/{spaceId}")
    suspend fun getFloorMapBySpaceId(
        @Path("spaceId") spaceId: Int
    ): FloorMapResponse

    /** space_id 기준 배치 가능한 온습도 센서 조회 (권장). */
    @GET("api/maps/space/{spaceId}/available-sensors")
    suspend fun getAvailableTempSensorsForMapBySpace(
        @Path("spaceId") spaceId: Int,
        @Query("map_id") mapId: Int? = null
    ): SensorListResponse

    @GET("api/maps/{mapId}/sensors")
    suspend fun getMapSensorPositions(
        @Path("mapId") mapId: Int
    ): SensorPositionListResponse

    /** Deprecated: getFloorMapBySpaceId() 를 사용하세요. */
    @GET("api/maps/{jetsonId}")
    suspend fun getFloorMap(
        @Path("jetsonId") jetsonId: Int
    ): FloorMapResponse

    /** Deprecated: getAvailableTempSensorsForMapBySpace() 를 사용하세요. */
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

    @GET("api/v1/workers/db")
    suspend fun getDbWorkers(
        @Query("is_manager") isManager: Int = 0
    ): List<WorkerDbResponse>

    @GET("api/v1/workers/db/{deptId}")
    suspend fun getDbWorker(
        @Path("deptId") deptId: Int
    ): WorkerDbResponse

    @POST("api/v1/workers/{deptId}/assign-heart-band")
    suspend fun assignHeartBandToWorker(
        @Path("deptId") deptId: Int,
        @Body request: AssignHeartBandRequest
    ): AssignHeartBandResponse

    @POST("api/v1/workers/{deptId}/unassign-sensor")
    suspend fun unassignWorkerSensor(
        @Path("deptId") deptId: Int
    ): UnassignSensorResponse
}