package com.example.ds_safer.data.repository

import com.example.ds_safer.domain.model.JetsonDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// object로 선언하여 앱 전체에서 단 하나만 존재하게 합니다 (싱글톤)
object JetsonRepository {
    private val _selectedJetson = MutableStateFlow<JetsonDevice?>(null)
    val selectedJetson = _selectedJetson.asStateFlow()

    // 젯슨 정보 저장
    fun setSelectedJetson(device: JetsonDevice) {
        _selectedJetson.value = device
    }

    // 현재 저장된 젯슨 정보 가져오기 (비동기 아닐 때용)
    fun getCurrentJetson(): JetsonDevice? = _selectedJetson.value
}