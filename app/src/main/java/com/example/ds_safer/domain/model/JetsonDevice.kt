package com.example.ds_safer.domain.model

import com.google.gson.annotations.SerializedName

// 1. 안드로이드 앱 화면(UI)에 리스트를 띄울 때 쓰는 내부용 그릇
data class JetsonDevice(
    val name: String,
    val ipAddress: String,
    val port: Int,
    val status: Boolean = false,
    val jetsonId: Int? = null,
    val spaceId: Int? = null,
    val spaceName: String? = null,
    val isRegistered: Boolean = false
)