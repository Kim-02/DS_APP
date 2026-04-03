package com.example.ds_safer.domain.model

import com.google.gson.annotations.SerializedName

// 1. 안드로이드 앱 화면(UI)에 리스트를 띄울 때 쓰는 내부용 그릇
data class JetsonDevice (
    val id: String,
    val name: String,                   // mDNS 서버 이름
    val ipAddress: String,              // 찾은 IP 주소
    val port: Int,
    val status: Boolean = true,         // (수정) 파이썬 기본값(True)에 맞춰 true로 변경

    var jetsonId: Int? = null
)