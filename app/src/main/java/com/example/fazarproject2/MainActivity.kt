package com.example.fazarproject2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.fazarproject2.domain.model.SunriseResults
import com.example.fazarproject2.ui.dashboard.DashboardScreen
import com.example.fazarproject2.ui.navigation.Dashboard
import com.example.fazarproject2.ui.navigation.SoundSelector
import com.example.fazarproject2.ui.navigation.SunriseDetails
import com.example.fazarproject2.ui.navigation.SunriseResultsType
import com.example.fazarproject2.ui.permission.PermissionGate
import com.example.fazarproject2.ui.sounds.SoundSelectionScreen
import com.example.fazarproject2.ui.sunrise.SunriseDetailsScreen
import com.example.fazarproject2.ui.theme.FazarProject2Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.reflect.typeOf

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
                    NavHost(navController = navController, startDestination = Dashboard) {
                        composable<Dashboard> {
                            DashboardScreen(
                                onNavigateToSoundSelector = { navController.navigate(SoundSelector) },
                                onNavigateToSunriseDetails = { results ->
                                    navController.navigate(SunriseDetails(results))
                                }
                            )
                        }
                        composable<SoundSelector> {
                            SoundSelectionScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable<SunriseDetails>(
                            typeMap = mapOf(typeOf<SunriseResults>() to SunriseResultsType)
                        ) { backStackEntry ->
                            val details: SunriseDetails = backStackEntry.toRoute()
                            SunriseDetailsScreen(
                                results = details.results,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
