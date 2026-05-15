package com.example.ds_safer

import AuthDataStore
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import AuthViewModel
import com.example.ds_safer.ui.navigation.NavGraph
import com.example.ds_safer.ui.screens.discovery.DiscoveryViewModel
import com.example.ds_safer.ui.theme.DSSaferTheme
import com.example.ds_safer.util.NotificationHelper
import com.example.ds_safer.util.nsd.NsdHelper

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 결과는 무시: 권한 거부 시 알림만 안 뜸 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 알림 채널 생성 (Android O+)
        NotificationHelper.createChannel(this)

        // Android 13+ 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val nsdHelper = NsdHelper(this)
        val authDataStore = AuthDataStore(this)

        setContent {
            DSSaferTheme {
                val navController = rememberNavController()

                val authViewModel: AuthViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return AuthViewModel(authDataStore) as T
                        }
                    }
                )

                val discoveryViewModel: DiscoveryViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return DiscoveryViewModel(nsdHelper) as T
                        }
                    }
                )

                Surface(color = MaterialTheme.colorScheme.background) {
                    NavGraph(
                        authViewModel = authViewModel,
                        navController = navController,
                        discoveryViewModel = discoveryViewModel,
                    )
                }
            }
        }
    }
}
