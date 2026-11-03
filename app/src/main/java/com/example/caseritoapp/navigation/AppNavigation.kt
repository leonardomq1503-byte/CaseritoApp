package com.example.caseritoapp.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.caseritoapp.ui.alerts.AlertsScreen
import com.example.caseritoapp.ui.auth.LoginScreen
import com.example.caseritoapp.ui.auth.RegistroScreen
import com.example.caseritoapp.ui.auth.VerificacionScreen
import com.example.caseritoapp.ui.home.HomeScreen
import com.example.caseritoapp.ui.map.MapScreen
import com.example.caseritoapp.ui.profile.ProfileScreen
import com.example.caseritoapp.ui.search.SearchScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.padding(paddingValues),
        enterTransition = { fadeIn() + slideInHorizontally { it / 14 } },
        exitTransition = { fadeOut() + slideOutHorizontally { -it / 14 } },
        popEnterTransition = { fadeIn() + slideInHorizontally { -it / 14 } },
        popExitTransition = { fadeOut() + slideOutHorizontally { it / 14 } }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen()
        }

        composable(Screen.Map.route) {
            MapScreen()
        }

        composable(Screen.Alerts.route) {
            AlertsScreen()
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        // Flujos de Autenticación
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Register.route) {
            RegistroScreen(
                onNavigateToVerification = { destination ->
                    navController.navigate(Screen.Verification.createRoute(destination))
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Verification.route,
            arguments = listOf(
                navArgument("destination") {
                    type = NavType.StringType
                    defaultValue = "correo"
                }
            )
        ) { backStackEntry ->
            val destination = backStackEntry.arguments?.getString("destination") ?: "correo"
            VerificacionScreen(
                destino = destination,
                onVerificationSuccess = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
