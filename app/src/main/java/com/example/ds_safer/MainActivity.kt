package com.example.ds_safer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.ds_safer.ui.navigation.NavGraph
import com.example.ds_safer.ui.theme.DSSaferTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DSSaferTheme {
                // navController 생성
                val navController = rememberNavController()

                Surface(color = MaterialTheme.colorScheme.background) {
                    // 우리가 만든 네비게이션 그래프 실행
                    NavGraph(navController = navController)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DSSaferTheme {
        Greeting("Android")
    }
}