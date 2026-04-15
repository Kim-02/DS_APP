package com.example.ds_safer

import AuthDataStore
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import AuthViewModel
import com.example.ds_safer.ui.navigation.NavGraph
import com.example.ds_safer.ui.screens.discovery.DiscoveryViewModel
import com.example.ds_safer.ui.theme.DSSaferTheme
import com.example.ds_safer.util.nsd.NsdHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 와이파이 레이더(NsdHelper) 및 데이터스토어 부품 생성
        val nsdHelper = NsdHelper(this)
        val authDataStore = AuthDataStore(this) // ★ 추가: 로컬 저장소(DataStore) 객체 생성

        setContent {
            DSSaferTheme {
                val navController = rememberNavController()

                // 2. AuthViewModel 공장(Factory) 가동
                // AuthDataStore를 뷰모델 안에 쏙 넣어줍니다.
                val authViewModel: AuthViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return AuthViewModel(authDataStore) as T
                        }
                    }
                )

                // 3. DiscoveryViewModel 공장(Factory) 가동
                val discoveryViewModel: DiscoveryViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return DiscoveryViewModel(nsdHelper, authDataStore) as T
                        }
                    }
                )

                Surface(color = MaterialTheme.colorScheme.background) {
                    // 4. 완성된 뷰모델 2개를 전체 지도(NavGraph)에 장착!
                    NavGraph(
                        authViewModel = authViewModel, // ★ 추가: 로그인 상태 관리를 위해 넘겨줌
                        navController = navController,
                        discoveryViewModel = discoveryViewModel
                    )
                }
            }
        }
    }
}