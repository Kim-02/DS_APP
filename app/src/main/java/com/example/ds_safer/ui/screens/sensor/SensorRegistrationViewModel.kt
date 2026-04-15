package com.example.ds_safer.ui.screens.sensor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ds_safer.data.api.RetrofitClient
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.domain.model.DiscoveredSensor
import com.example.ds_safer.domain.model.SensorRegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SensorRegistrationViewModel : ViewModel() {

    // 1. 대기 중인(발견된) 센서 목록을 담을 변수
    private val _readySensors = MutableStateFlow<List<DiscoveredSensor>>(emptyList())
    val readySensors = _readySensors.asStateFlow()

    // 화면이 켜지자마자 딱 하나, "새 센서 목록"만 가져옵니다!
    init {
        fetchReadySensors()
    }

    // 3. 젯슨 주변에서 대기 중인 새 센서 목록 긁어오기
    fun fetchReadySensors() {
        val device = JetsonRepository.selectedJetson.value ?: return
        viewModelScope.launch {
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                // 1. API를 호출해서 JSON 전체 응답 객체를 통째로 받음
                val response = service.getDiscoverSensors()

                // 2. 껍데기 안에서 실제 센서 리스트(discoveredSensors)만 꺼내서 뷰모델 상태 업데이트
                _readySensors.value = response.data

            } catch (e: Exception) {
                // 에러 발생 시 일단 빈 리스트 유지
                _readySensors.value = emptyList()
                e.printStackTrace() // 디버깅을 위해 에러 로그 출력 (필수)
            }
        }
    }

    // 🔄 수정됨: 여러 개의 센서를 한 번의 API 호출로 통째로 등록!
    fun registerMultipleSensors(selectedList: List<DiscoveredSensor>) {
        if (selectedList.isEmpty()) return // 선택된 게 없으면 요청 안 함

        val device = JetsonRepository.selectedJetson.value ?: return
        val actualJetsonId = device.jetsonId ?: 1 // 만약 null이면 기본값 1

        viewModelScope.launch {
            try {
                val baseUrl = "http://${device.ipAddress}:${device.port}/"
                val service = RetrofitClient.createService(baseUrl)

                // 1. 서버에 보낼 JSON 껍데기 조립 (파이썬 서버가 기대하는 "Jetson-1" 문자열 형태로 가공)
                val requestBody = SensorRegisterRequest(
                    jetsonId = "jetson-$actualJetsonId",
                    selectedSensors = selectedList // 선택된 리스트 통째로 삽입!
                )

                // 2. 서버로 POST 요청 딱 "한 번만" 쏘기! (for문 제거됨)
                val response = service.registerSensor(requestBody)

                // 3. 등록 성공 처리
                if (response.status == "success") {
                    println("✅ 센서 다중 등록 성공: ${response.message}")
                    // 등록이 다 끝났으면, 화면에서 등록된 애들을 없애기 위해 목록을 한 번 더 새로고침!
                    fetchReadySensors()
                } else {
                    println("❌ 서버 응답 실패: ${response.message}")
                }

            } catch (e: Exception) {
                // 에러 처리
                e.printStackTrace()
            }
        }
    }
}
