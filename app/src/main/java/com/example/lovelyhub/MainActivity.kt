package com.example.lovelyhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lovelyhub.ui.auth.AuthScreen
import com.example.lovelyhub.ui.auth.AuthState
import com.example.lovelyhub.ui.auth.AuthViewModel
import com.example.lovelyhub.ui.home.HomeScreen
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
fun LovelyHubApp(authViewModel: AuthViewModel = viewModel()) {
    var isSplashFinished by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

    if (!isSplashFinished) {
        SplashScreen(
            onSplashFinished = {
                isSplashFinished = true
            }
        )
    } else {
        if (authState is AuthState.Success) {
            HomeScreen(
                viewModel = authViewModel,
                onSignOut = {
                }
            )
        } else {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                }
            )
        }
    }
}
