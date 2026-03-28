package com.example.ds_safer.domain.model

data class JetsonDevice (
    val name: String,                   // mDNS 서버 이름
    val ipAddress: String,              // 찾은 IP 주소
    val port: Int,
    val isConnected: Boolean = false
)