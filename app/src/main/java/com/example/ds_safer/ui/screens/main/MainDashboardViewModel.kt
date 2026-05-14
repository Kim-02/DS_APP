package com.example.ds_safer.ui.screens.main

import androidx.lifecycle.ViewModel
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.JetsonDevice
import kotlinx.coroutines.flow.StateFlow

class MainDashboardViewModel : ViewModel() {
    val selectedJetson: StateFlow<JetsonDevice?> = JetsonRepository.selectedJetson
}