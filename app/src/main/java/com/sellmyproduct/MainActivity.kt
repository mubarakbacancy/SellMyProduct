package com.sellmyproduct

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sellmyproduct.navigation.NavRoutes
import com.sellmyproduct.ui.home.HomeScreen
import com.sellmyproduct.ui.login.LoginScreen
import com.sellmyproduct.ui.register.RegisterScreen
import com.sellmyproduct.ui.splash.SplashScreen
import com.sellmyproduct.ui.theme.SellMyProductTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SellMyProductTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.SPLASH,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(NavRoutes.SPLASH) {
                        SplashScreen(
                            onNavigateToLogin = {
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                                }
                            },
                            onNavigateToHome = {
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable(NavRoutes.LOGIN) {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(NavRoutes.LOGIN) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(NavRoutes.REGISTER)
                            }
                        )
                    }
                    
                    composable(NavRoutes.REGISTER) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(NavRoutes.REGISTER) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }
                    
                    composable(NavRoutes.HOME) {
                        HomeScreen(
                            onLogout = {
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(NavRoutes.HOME) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}