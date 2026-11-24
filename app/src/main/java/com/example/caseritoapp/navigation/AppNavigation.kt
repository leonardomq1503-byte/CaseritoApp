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
import com.example.caseritoapp.ui.profile.ReservasScreen
import com.example.caseritoapp.ui.profile.FavoritesScreen
import com.example.caseritoapp.ui.search.SearchScreen
import com.example.caseritoapp.ui.welcome.WelcomeScreen
import com.example.caseritoapp.ui.store.StoreRegistrationScreen
import com.example.caseritoapp.ui.store.MerchantScreen
import com.example.caseritoapp.ui.details.ProductDetailScreen
import com.example.caseritoapp.ui.details.StoreDetailScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    paddingValues: PaddingValues,
    startDestination: String = Screen.Home.route,
    onWelcomeFinished: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.padding(paddingValues),
        enterTransition = { fadeIn() + slideInHorizontally { it / 14 } },
        exitTransition = { fadeOut() + slideOutHorizontally { -it / 14 } },
        popEnterTransition = { fadeIn() + slideInHorizontally { -it / 14 } },
        popExitTransition = { fadeOut() + slideOutHorizontally { it / 14 } }
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onCreateAccount = {
                    onWelcomeFinished()
                    navController.navigate(Screen.Register.route)
                },
                onSignIn = {
                    onWelcomeFinished()
                    navController.navigate(Screen.Login.route)
                },
                onExplore = {
                    onWelcomeFinished()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToProductDetail = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onNavigateToStoreDetail = { id -> navController.navigate(Screen.StoreDetail.createRoute(id)) }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToProductDetail = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onNavigateToStoreDetail = { id -> navController.navigate(Screen.StoreDetail.createRoute(id)) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(onNavigateToStoreDetail = { id -> navController.navigate(Screen.StoreDetail.createRoute(id)) })
        }

        composable(Screen.Alerts.route) {
            AlertsScreen(onNavigateToLogin = { navController.navigate(Screen.Login.route) })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToReservations = { navController.navigate(Screen.Reservations.route) },
                onNavigateToStoreRegistration = { navController.navigate(Screen.Merchant.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProduct = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onNavigateToStore = { id -> navController.navigate(Screen.StoreDetail.createRoute(id)) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.StoreRegistration.route) {
            StoreRegistrationScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Merchant.route) {
            MerchantScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRegistration = { navController.navigate(Screen.StoreRegistration.route) }
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { entry ->
            val productId = entry.arguments?.getLong("productId") ?: return@composable
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStore = { id -> navController.navigate(Screen.StoreDetail.createRoute(id)) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) }
            )
        }

        composable(
            route = Screen.StoreDetail.route,
            arguments = listOf(navArgument("storeId") { type = NavType.LongType })
        ) { entry ->
            val storeId = entry.arguments?.getLong("storeId") ?: return@composable
            StoreDetailScreen(
                storeId = storeId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProduct = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) }
            )
        }

        composable(Screen.Reservations.route) {
            ReservasScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        // Flujos de Autenticación
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
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
                onRegistrationSuccess = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
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
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
