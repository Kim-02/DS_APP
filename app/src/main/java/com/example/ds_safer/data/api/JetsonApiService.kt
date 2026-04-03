package com.example.ds_safer.data.api

import com.example.ds_safer.domain.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


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

    // 1. 젯슨 헬스 체크 (단순 생존 확인용)
    @GET("/")
    suspend fun checkHealth(): Response<ResponseBody>

    @GET("api/jetson")
    suspend fun getJetsonInfo(): JetsonInfoResponse

    @POST("api/jetson/register")
    suspend fun registerJetson(
        @Body request: JetsonRegisterRequest
    ): Response<JetsonRegisterResponse>

    // 광고된 센서 띄우기
    @GET("api/sensors/discovered")
    suspend fun getDiscoverSensors(): DiscoveredSensorsResponse

    // 4. 센서 등록 요청
    @POST("api/sensors/register")
    suspend fun registerSensor(
        @Body request: SensorRegisterRequest
    ): SimpleResponse

    // 5. CCTV 카메라 등록 요청
    @POST("api/cameras/register")
    suspend fun registerCctv(
        @Body request: CameraCreate
    ): CameraRegisterResponse

    // 🆕 1. 등록된 CCTV 목록 긁어오기
    @GET("api/cameras") // URL은 서버 명세 확인!
    suspend fun getRegisteredCameras(): CctvListResponse

    // 🆕 2. 등록된 센서 목록 긁어오기
    @GET("api/sensors")
    suspend fun getRegisteredSensors(): SensorListResponse
}