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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.caseritoapp.ui.alerts.AlertsScreen
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
            ProfileScreen()
        }
    }
}
