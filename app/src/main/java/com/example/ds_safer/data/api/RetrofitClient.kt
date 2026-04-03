package com.example.ds_safer.data.api

import com.example.ds_safer.ui.screens.main.JetsonDeviceItem
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS) // 설계도 폐쇄망 특성상 짧게 설정
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    // 탐색된 젯슨의 IP에 따라 dynamic하게 API 서비스를 생성하는 함수
    fun createService(baseUrl: String): JetsonApiService {
        // baseUrl 형식 예시: http://192.168.1.100:8080/
        val finalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(finalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JetsonApiService::class.java)
    }

    fun createWorkerService(baseUrl: String): JetsonApiService {
        val finalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(finalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JetsonApiService::class.java) // 🌟 문제의 { } 이것만 딱 뺐습니다!
    }
}