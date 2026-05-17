package com.example.ds_safer.data.api

import com.example.ds_safer.domain.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
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

    // ==========================================
    // Health / Legacy Jetson API
    // ==========================================

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

    /** Jetson 완전 삭제 (연결된 sensor + CCTV runtime 포함). */
    @DELETE("/api/v1/jetsons/{jetsonId}")
    suspend fun deleteJetsonV1(
        @Path("jetsonId") jetsonId: Int
    ): JetsonUnregisterResponse

    // ==========================================
    // Dashboard API
    // ==========================================

    @GET("/api/v1/dashboard/summary")
    suspend fun getDashboardSummary(
        @Query("space_id") spaceId: Int
    ): DashboardSummaryResponse

    @GET("/api/v1/dashboard/recent-alerts")
    suspend fun getRecentAlerts(
        @Query("space_id") spaceId: Int,
        @Query("limit") limit: Int = 20
    ): RecentAlertsResponse

    @PATCH("/api/v1/dashboard/alerts/{event_id}/read")
    suspend fun markAlertAsRead(
        @Path("event_id") eventId: Int,
        @Query("space_id") spaceId: Int? = null
    ): MarkAsReadResponse

    @GET("/api/v1/dashboard/sensors")
    suspend fun getDashboardSensors(
        @Query("space_id") spaceId: Int
    ): DashboardSensorsResponse

    @GET("/api/v1/dashboard/cctvs")
    suspend fun getDashboardCctvs(
        @Query("space_id") spaceId: Int
    ): DashboardCctvsResponse

    @GET("/api/v1/dashboard/workers")
    suspend fun getDashboardWorkers(
        @Query("space_id") spaceId: Int
    ): DashboardWorkersResponse

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

    @POST("/api/v1/cctv/cameras/register")
    suspend fun registerCctv(
        @Body request: AppCameraRegisterRequest
    ): CameraOutResponse

    @DELETE("/api/v1/cctv/cameras/{sensorId}")
    suspend fun deleteCamera(
        @Path("sensorId") sensorId: Int
    ): Response<Unit>

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

    /** space_id 기준 배치 가능한 CCTV 조회. */
    @GET("api/maps/space/{spaceId}/available-cctvs")
    suspend fun getAvailableCctvsForMapBySpace(
        @Path("spaceId") spaceId: Int,
        @Query("map_id") mapId: Int? = null
    ): AvailableCctvListResponse

    @GET("api/maps/{mapId}/sensors")
    suspend fun getMapSensorPositions(
        @Path("mapId") mapId: Int
    ): SensorPositionListResponse


    @POST("api/maps/sensors/position")
    suspend fun saveSensorPosition(
        @Body request: SaveSensorPositionRequest
    ): SimpleResponse

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
}