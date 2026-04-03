package com.example.ds_safer.domain.model

import com.google.gson.annotations.SerializedName

// 1. 서버 응답 전체를 감싸는 껍데기 (새로 추가!)
data class CctvListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<RegisteredCamera>
)

// 2. 실제 CCTV 한 대의 정보 (파이썬 코드에 맞춰서 필드 수정)
data class RegisteredCamera(
    @SerializedName("ip_address") val ipAddress: String,
    @SerializedName("sen_name") val senName: String?, // 이름이 없을 수도 있으니 ?(Nullable) 처리
    @SerializedName("sen_locate") val senLocate: String?,
    @SerializedName("health") val health: String?
)