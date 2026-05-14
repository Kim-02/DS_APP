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
import com.example.ds_safer.ui.screens.jetson.JetsonSpaceRegisterScreen
import com.example.ds_safer.ui.screens.jetson.JetsonSpaceRegisterViewModel
import com.example.ds_safer.ui.screens.main.MainDashboardScreen
import com.example.ds_safer.ui.screens.report.EventReportScreen
import com.example.ds_safer.ui.screens.sensor.SensorRegistrationScreen
import com.example.ds_safer.ui.screens.sensor.SensorRegistrationViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object JetsonDetail : Screen("detail")

    object JetsonSpaceRegister : Screen("jetson_space_register")

    object SensorList : Screen("sensor_list")
    object CctvList : Screen("cctv_list")

    object SensorRegistration : Screen("sensor_register")
    object CctvRegistration : Screen("cctv_register")

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
                onNavigateToJetsonSpaceRegister = {
                    navController.navigate(Screen.JetsonSpaceRegister.route)
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

        composable(Screen.JetsonSpaceRegister.route) {
            val jetsonSpaceRegisterViewModel: JetsonSpaceRegisterViewModel = viewModel()

            JetsonSpaceRegisterScreen(
                discoveryViewModel = discoveryViewModel,
                viewModel = jetsonSpaceRegisterViewModel,
                onBackClick = {
                    discoveryViewModel.loadRegisteredJetsonsFromDiscoveredServers()
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    discoveryViewModel.loadRegisteredJetsonsFromDiscoveredServers()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.JetsonDetail.route) {
            val currentDevice by JetsonRepository.selectedJetson.collectAsState()

            if (currentDevice != null) {
                JetsonDetailScreen(
                    device = currentDevice!!,
                    onDisconnectClick = {
                        JetsonRepository.clear()
                        discoveryViewModel.loadRegisteredJetsonsFromDiscoveredServers()

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

        composable(Screen.FloorMap.route) {
            val floorMapViewModel: FloorMapViewModel = viewModel()

            FloorMapScreen(
                viewModel = floorMapViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

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

        composable(Screen.SensorRegistration.route) {
            val sensorViewModel: SensorRegistrationViewModel = viewModel()

            SensorRegistrationScreen(
                viewModel = sensorViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

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

        composable(Screen.CctvRegistration.route) {
            val cctvViewModel: com.example.ds_safer.ui.screens.cctv.CctvRegistrationViewModel =
                viewModel()

            com.example.ds_safer.ui.screens.cctv.CctvRegistrationScreen(
                viewModel = cctvViewModel,
                onSuccess = {
                    navController.navigate(Screen.CctvList.route) {
                        popUpTo(Screen.CctvList.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

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