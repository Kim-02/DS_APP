package com.example.ds_safer.ui.navigation

import AuthViewModel
import LoginScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ds_safer.data.repository.JetsonRepository
import com.example.ds_safer.ui.screens.detail.JetsonDetailScreen
import com.example.ds_safer.ui.screens.discovery.DiscoveryViewModel
import com.example.ds_safer.ui.screens.main.MainDashboardScreen
import com.example.ds_safer.ui.screens.sensor.SensorRegistrationScreen
import com.example.ds_safer.ui.screens.sensor.SensorRegistrationViewModel

// 1️⃣ Screen 라우트 (완벽함!)
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object JetsonDetail : Screen("detail")

    // 목록(관리) 화면 라우트
    object SensorList : Screen("sensor_list")
    object CctvList : Screen("cctv_list")

    // 등록 화면 라우트
    object SensorRegistration : Screen("sensor_register")
    object CctvRegistration : Screen("cctv_register")
}

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    navController: NavHostController,
    discoveryViewModel: DiscoveryViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    if (isLoggedIn == null) return

    val startDestination = if (isLoggedIn == true) Screen.Main.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {

        // ==========================================
        // 0. 로그인 화면
        // ==========================================
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // 1. 메인 대시보드 화면
        // ==========================================
        composable(Screen.Main.route) {
            MainDashboardScreen(
                viewModel = discoveryViewModel,
                authViewModel = authViewModel,
                onNavigateToDetail = { selectedDevice ->
                    JetsonRepository.selectJetson(selectedDevice)
                    navController.navigate(Screen.JetsonDetail.route)
                },
                onLogoutClick = {
                    authViewModel.logout()        // 1. 기기 저장소 초기화
                    JetsonRepository.clear()      // 2. 선택된 젯슨 정보(RAM) 초기화

                    // 3. 메인 화면의 백스택(기록)을 싹 날리면서 로그인 화면으로 이동
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // 2. 젯슨 상세 화면 (리모컨)
        // ==========================================
        composable(Screen.JetsonDetail.route) {
            val currentDevice = JetsonRepository.selectedJetson.value
            if (currentDevice != null) {
                JetsonDetailScreen(
                    device = currentDevice,
                    onDisconnectClick = {
                        JetsonRepository.clear()
                        navController.popBackStack(Screen.Main.route, inclusive = false)
                    },
                    // ★ 수정: Register가 아니라 List 화면으로 이동하게 변경!
                    // (주의: JetsonDetailScreen.kt 파일 내부에서도 매개변수 이름을 onNavigateToSensorList 등으로 맞춰주는 게 좋아)
                    onNavigateToSensorRegister = {
                        navController.navigate(Screen.SensorList.route)
                    },
                    onNavigateToCctvRegister = {
                        navController.navigate(Screen.CctvList.route)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ==========================================
        // 3-1. 센서 목록(관리) 화면 (★ 신규 추가!)
        // ==========================================
        composable(Screen.SensorList.route) {
            val sensorListViewModel: com.example.ds_safer.ui.screens.sensor.SensorListViewModel = viewModel()

            com.example.ds_safer.ui.screens.sensor.SensorListScreen(
                viewModel = sensorListViewModel,
                onNavigateToRegister = { navController.navigate(Screen.SensorRegistration.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ==========================================
        // 3-2. 센서 등록 화면
        // ==========================================
        composable(Screen.SensorRegistration.route) {
            val sensorViewModel: SensorRegistrationViewModel = viewModel()
            SensorRegistrationScreen(
                viewModel = sensorViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // ==========================================
        // 4-1. CCTV 목록(관리) 화면 (★ 신규 추가!)
        // ==========================================
        composable(Screen.CctvList.route) {
            val cctvListViewModel: com.example.ds_safer.ui.screens.cctv.CctvListViewModel = viewModel()

            com.example.ds_safer.ui.screens.cctv.CctvListScreen(
                viewModel = cctvListViewModel,
                onNavigateToRegister = { navController.navigate(Screen.CctvRegistration.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ==========================================
        // 4-2. CCTV 등록 화면
        // ==========================================
        composable(Screen.CctvRegistration.route) {
            val cctvViewModel: com.example.ds_safer.ui.screens.cctv.CctvRegistrationViewModel = viewModel()

            com.example.ds_safer.ui.screens.cctv.CctvRegistrationScreen(
                viewModel = cctvViewModel,
                onSuccess = {
                    // 등록 성공하면 이전 화면(CCTV 목록 화면)으로 돌아가기
                    navController.popBackStack()
                }
            )
        }
    }
}