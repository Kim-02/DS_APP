package com.example.ds_safer.data.repository

import com.example.ds_safer.domain.model.JetsonDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// 앱 전체에서 젯슨 기기 정보를 공유하기 위한 '전역 보관함 (Singleton)'
object JetsonRepository {

    // 1. 현재 선택된 기기를 담고 있는 상자 (초기값은 텅 빔)
    private val _selectedJetson = MutableStateFlow<JetsonDevice?>(null)
    val selectedJetson: StateFlow<JetsonDevice?> = _selectedJetson.asStateFlow()

    // ★ 에러 원인 1: 기기 저장 함수 추가
    fun selectJetson(device: JetsonDevice) {
        _selectedJetson.value = device
    }

    // ★ 에러 원인 2: 기기 비우기(연결 해제) 함수 추가
    fun clear() {
        _selectedJetson.value = null
    }
}