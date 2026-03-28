package com.example.ds_safer.domain.model

data class CctvRegisterRequest(
    val name: String,
    val ip_address: String,
    val port: Int = 554, // RTSP 기본 포트
    val id: String,
    val pw: String
)

data class CctvResponse(
    val status: String, // "success" or "fail"
    val message: String? = null
)