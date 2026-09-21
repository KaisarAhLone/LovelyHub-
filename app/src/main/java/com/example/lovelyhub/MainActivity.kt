package com.example.lovelyhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.lovelyhub.ui.splash.SplashScreen
import com.example.lovelyhub.ui.theme.LovelyHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LovelyHubTheme {
                LovelyHubApp()
            }
        }
    }
}

@Composable
fun LovelyHubApp() {
    SplashScreen()
}
