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
import com.example.ds_safer.data.websocket.WebSocketManager
import com.example.ds_safer.ui.screens.detail.JetsonDetailScreen
import com.example.ds_safer.ui.screens.discovery.DiscoveryViewModel
import com.example.ds_safer.ui.screens.floormap.FloorMapScreen
import com.example.ds_safer.ui.screens.floormap.FloorMapViewModel
import com.example.ds_safer.ui.screens.main.MainDashboardScreen
import com.example.ds_safer.ui.screens.report.EventReportScreen
import com.example.ds_safer.ui.screens.sensor.SensorRegistrationScreen
import com.example.ds_safer.ui.screens.sensor.SensorRegistrationViewModel

// 1️⃣ Screen 라우트
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

    // 평면도 화면 라우트
    object FloorMap : Screen("floor_map")

    object EventReport : Screen("event_report/{eventId}") {
        fun createRoute(eventId: Int) = "event_report/$eventId"
    }
}

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    navController: NavHostController,
    discoveryViewModel: DiscoveryViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    if (isLoggedIn == null) return

    val startDestination = if (isLoggedIn == true) {
        Screen.Main.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ==========================================
        // 0. 로그인 화면
        // ==========================================
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
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
                onNavigateToReport = { eventId ->
                    navController.navigate(Screen.EventReport.createRoute(eventId))
                },
                onLogoutClick = {
                    authViewModel.logout()
                    JetsonRepository.clear()
                    WebSocketManager.disconnect()

                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // ==========================================
        // 2. 젯슨 상세 화면
        // ==========================================
        composable(Screen.JetsonDetail.route) {
            val currentDevice by JetsonRepository.selectedJetson.collectAsState()

            if (currentDevice != null) {
                JetsonDetailScreen(
                    device = currentDevice!!,
                    onDisconnectClick = {
                        JetsonRepository.clear()
                        navController.popBackStack(
                            route = Screen.Main.route,
                            inclusive = false
                        )
                    },
                    onNavigateToSensorRegister = {
                        navController.navigate(Screen.SensorList.route)
                    },
                    onNavigateToCctvRegister = {
                        navController.navigate(Screen.CctvList.route)
                    },
                    onNavigateToFloorMap = {
                        navController.navigate(Screen.FloorMap.route)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ==========================================
        // 2-1. 평면도 화면
        // ==========================================
        composable(Screen.FloorMap.route) {
            val floorMapViewModel: FloorMapViewModel = viewModel()

            FloorMapScreen(
                viewModel = floorMapViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // ==========================================
        // 3-1. 센서 목록(관리) 화면
        // ==========================================
        composable(Screen.SensorList.route) {
            val sensorListViewModel: com.example.ds_safer.ui.screens.sensor.SensorListViewModel =
                viewModel()

            com.example.ds_safer.ui.screens.sensor.SensorListScreen(
                viewModel = sensorListViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.SensorRegistration.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
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
        // 4-1. CCTV 목록(관리) 화면
        // ==========================================
        composable(Screen.CctvList.route) {
            val cctvListViewModel: com.example.ds_safer.ui.screens.cctv.CctvListViewModel =
                viewModel()

            com.example.ds_safer.ui.screens.cctv.CctvListScreen(
                viewModel = cctvListViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.CctvRegistration.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // ==========================================
        // 4-2. CCTV 등록 화면
        // ==========================================
        composable(Screen.CctvRegistration.route) {
            val cctvViewModel: com.example.ds_safer.ui.screens.cctv.CctvRegistrationViewModel =
                viewModel()

            com.example.ds_safer.ui.screens.cctv.CctvRegistrationScreen(
                viewModel = cctvViewModel,
                onSuccess = {
                    /*
                     * 단순 popBackStack()만 하면 기존 CctvListViewModel이 살아 있어서
                     * 방금 등록한 CCTV 목록이 바로 갱신되지 않을 수 있습니다.
                     *
                     * 따라서 기존 cctv_list를 스택에서 제거하고,
                     * 새 cctv_list로 다시 진입시켜 fetchCameras()가 다시 실행되게 합니다.
                     */
                    navController.navigate(Screen.CctvList.route) {
                        popUpTo(Screen.CctvList.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ==========================================
        // 5. 이벤트 보고서 화면
        // ==========================================
        composable(Screen.EventReport.route) { backStackEntry ->
            val eventIdString = backStackEntry.arguments?.getString("eventId") ?: "0"
            val eventId = eventIdString.toIntOrNull() ?: 0

            EventReportScreen(
                eventId = eventId,
                authViewModel = authViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onSubmitSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}