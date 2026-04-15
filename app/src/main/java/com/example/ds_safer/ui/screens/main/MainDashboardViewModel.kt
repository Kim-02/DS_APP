package com.example.ds_safer.ui.screens.main

import androidx.lifecycle.ViewModel
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.JetsonDevice
import kotlinx.coroutines.flow.StateFlow

class MainDashboardViewModel : ViewModel() {
    // 저장소의 데이터를 그대로 UI에 노출
    val selectedJetson: StateFlow<JetsonDevice?> = JetsonRepository.selectedJetson
}