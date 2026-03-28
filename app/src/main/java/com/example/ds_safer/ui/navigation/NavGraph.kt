package com.example.ds_safer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ds_safer.ui.screens.cctv.CctvRegistrationScreen
import com.example.ds_safer.ui.screens.discovery.DiscoveryScreen
import com.example.ds_safer.ui.screens.main.MainDashboardScreen
import com.example.ds_safer.ui.screens.sensor.SensorRegistrationScreen

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object JetsonDiscovery : Screen("jetson_discovery")
    object SensorRegistration : Screen("sensor_reg")
    object CctvRegistration : Screen("cctv_reg")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainDashboardScreen(
                onNavigateToJetson = { navController.navigate(Screen.JetsonDiscovery.route) },
                onNavigateToSensor = { navController.navigate(Screen.SensorRegistration.route) },
                onNavigateToCctv = { navController.navigate(Screen.CctvRegistration.route) }
            )
        }

        // [수정된 부분] navController를 DiscoveryScreen에 넘겨줍니다.
        composable(Screen.JetsonDiscovery.route) {
            DiscoveryScreen(navController = navController)
        }

        composable(Screen.SensorRegistration.route) {
            SensorRegistrationScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CctvRegistration.route) {
            CctvRegistrationScreen(
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}