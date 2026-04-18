package com.example.fazarproject2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fazarproject2.ui.permission.PermissionGate
import com.example.fazarproject2.ui.dashboard.DashboardScreen
import com.example.fazarproject2.ui.sounds.SoundSelectionScreen
import com.example.fazarproject2.ui.theme.FazarProject2Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FazarProject2Theme {
                PermissionGate {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") {
                            DashboardScreen(
                                onNavigateToSoundSelector = { navController.navigate("sound_selector") }
                            )
                        }
                        composable("sound_selector") {
                            SoundSelectionScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
