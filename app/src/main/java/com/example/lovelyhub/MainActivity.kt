package com.example.lovelyhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lovelyhub.data.local.LocalUserCache
import com.example.lovelyhub.ui.auth.AuthScreen
import com.example.lovelyhub.ui.auth.AuthState
import com.example.lovelyhub.ui.auth.AuthViewModel
import com.example.lovelyhub.ui.home.HomeScreen
import com.example.lovelyhub.ui.onboarding.OnboardingScreen
import com.example.lovelyhub.ui.splash.SplashScreen
import com.example.lovelyhub.ui.theme.LovelyHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())

        setContent {
            LovelyHubTheme {
                LovelyHubApp()
            }
        }
    }
}

@Composable
fun LovelyHubApp(authViewModel: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    var isSplashFinished by remember { mutableStateOf(false) }
    var hasCompletedOnboarding by remember {
        mutableStateOf(LocalUserCache(context).hasCompletedOnboarding())
    }
    var hasAcceptedDisclaimer by remember {
        mutableStateOf(LocalUserCache(context).hasAcceptedDisclaimer())
    }
    val authState by authViewModel.authState.collectAsState()

    if (!isSplashFinished) {
        SplashScreen(
            onSplashFinished = {
                isSplashFinished = true
            }
        )
    } else if (!hasCompletedOnboarding) {
        OnboardingScreen(
            onFinishOnboarding = {
                LocalUserCache(context).setCompletedOnboarding(true)
                hasCompletedOnboarding = true
            }
        )
    } else if (!hasAcceptedDisclaimer) {
        LovelyHubDisclaimerDialog(
            onAccepted = {
                LocalUserCache(context).setAcceptedDisclaimer(true)
                hasAcceptedDisclaimer = true
            }
        )
    } else {
        if (authState is AuthState.Success) {
            HomeScreen(
                viewModel = authViewModel,
                onSignOut = {
                    authViewModel.signOut()
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

@Composable
fun LovelyHubDisclaimerDialog(
    onAccepted: () -> Unit
) {
    var isChecked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFFEC4899),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Safety & Terms Disclaimer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1E2D)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF8E1))
                        .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Important Notice for All Users & Members Using LovelyHub App!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78350F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "1. Public Visibility of Contact Info:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1E1E2D)
                )
                Text(
                    text = "Any contact details you provide (including Business Name, Phone Number, WhatsApp Number, and Address/Location) will be publicly visible to enable direct calls, WhatsApp chats, and orders.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "2. Authorized Business & Safety Permits:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1E1E2D)
                )
                Text(
                    text = "All Sellers, Restaurants, Rental Providers & Service Providers must be authorized and hold valid permits or food safety certificates where applicable. Misleading information or unauthorized listings are strictly forbidden.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "3. Payment Safety Guideline (DO NOT PAY ONLINE!):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFFD32F2F)
                )
                Text(
                    text = "🚫 DO NOT MAKE ONLINE PAYMENTS IN ADVANCE! If anyone asks you to pay money online before delivering your food, product, or service, DO NOT PAY. Always meet the seller/provider in person, inspect your order, and pay only after meeting them.",
                    fontSize = 12.sp,
                    color = Color(0xFFB71C1C),
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "4. Zero Liability Disclaimer:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1E1E2D)
                )
                Text(
                    text = "LovelyHub is a direct connecting platform and does NOT collect online payments or mediate deals. If you transfer money online in advance, LovelyHub is NOT responsible for any loss or fraud.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isChecked = !isChecked }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEC4899))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I have read, understood & agree to all LovelyHub terms, payment guidelines & disclaimers.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E2D),
                        lineHeight = 15.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAccepted,
                enabled = isChecked,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I Agree & Continue", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp)
    )
}
