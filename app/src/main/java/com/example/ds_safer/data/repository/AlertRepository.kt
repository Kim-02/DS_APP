package com.example.ds_safer.data.repository

import com.example.ds_safer.domain.model.HazardAlert
import com.example.ds_safer.domain.model.RecentAlertDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AlertRepository {

    private val _alerts = MutableStateFlow<List<RecentAlertDto>>(emptyList())
    val alerts = _alerts.asStateFlow()

    /** API에서 불러온 알림 목록으로 전체 교체 (중복 제거 후 최신순 정렬). */
    fun setAlerts(remote: List<RecentAlertDto>) {
        val current = _alerts.value
        _alerts.value = merge(remote, current)
    }

    /** WebSocket으로 수신한 새 알림을 맨 앞에 추가 (중복 event_id 방지). */
    fun prependAlert(alert: RecentAlertDto) {
        val current = _alerts.value
        if (alert.eventId != null && current.any { it.eventId == alert.eventId }) return
        _alerts.value = listOf(alert) + current
    }

    /** HazardAlert 수신 즉시 RecentAlertDto로 변환해 prepend. */
    fun prependFromHazardAlert(hazard: HazardAlert) {
        val alert = RecentAlertDto(
            eventId  = hazard.eventId?.toInt(),
            spaceId  = hazard.spaceId,
            title    = hazard.title ?: hazard.evCodeName ?: "온습도 위험 감지",
            message  = hazard.message,
            level    = hazard.level ?: "warning",
            source   = hazard.source ?: "vlm",
            cameraName = hazard.cameraName,
            createdAt  = hazard.createdAt ?: hazard.eventTime,
            isRead   = false,
        )
        prependAlert(alert)
    }

    /** 특정 알림을 읽음 처리 (로컬 state 즉시 반영). */
    fun markAsRead(eventId: Int) {
        _alerts.value = _alerts.value.map {
            if (it.eventId == eventId) it.copy(isRead = true) else it
        }
    }

    /** 읽지 않은 알림 수. */
    fun unreadCount(): Int = _alerts.value.count { it.isRead != true }

    /** 현재 spaceId (알림 목록 첫 항목 기준). */
    fun currentSpaceId(): Int? = _alerts.value.firstOrNull()?.spaceId

    /** 알림 초기화 (로그아웃 등). */
    fun clear() {
        _alerts.value = emptyList()
    }

    private fun merge(
        remote: List<RecentAlertDto>,
        local: List<RecentAlertDto>,
    ): List<RecentAlertDto> =
        (local + remote)
            .distinctBy { it.eventId?.toString() ?: "${it.createdAt}-${it.message}" }
            .sortedByDescending { it.createdAt ?: "" }
}
