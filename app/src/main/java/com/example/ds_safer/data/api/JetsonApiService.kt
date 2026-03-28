package com.example.ds_safer.data.api

import com.example.ds_safer.domain.model.CctvRegisterRequest
import com.example.ds_safer.domain.model.CctvResponse
import com.example.ds_safer.domain.model.SensorInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

// 서버 응답 데이터를 담을 DTO (서버 응답 형식에 맞게 수정 필요)
data class HealthResponse(
    val status: String // 예: "ok", "running"
)

data class SensorRegisterRequest(
    val sensor_id: String,
    val action: String = "register"
)

// 젯슨의 생사 여부
interface JetsonApiService {
    @GET("api/health")
    suspend fun checkHealth(): HealthResponse

    @GET("api/sensor/ready")
    suspend fun getReadySensors(): List<SensorInfo>

    @POST("api/sensor/register")
    suspend fun registerSensor(@Body request: SensorRegisterRequest): Response<Unit>

    @POST("api/cameras/register")
    suspend fun registerCctv(@Body request: CctvRegisterRequest): CctvResponse
}