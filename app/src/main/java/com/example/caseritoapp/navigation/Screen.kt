package com.example.caseritoapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "Inicio",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Welcome : Screen("welcome", "Bienvenido", Icons.Filled.Home, Icons.Outlined.Home)
    data object StoreRegistration : Screen("store-registration", "Registrar tienda", Icons.Filled.Person, Icons.Outlined.Person)
    data object Merchant : Screen("merchant", "Panel de mi tienda", Icons.Filled.Person, Icons.Outlined.Person)
    data object ProductDetail : Screen("product-detail/{productId}", "Detalle del producto", Icons.Filled.Search, Icons.Outlined.Search) {
        fun createRoute(productId: Long) = "product-detail/$productId"
    }
    data object StoreDetail : Screen("store-detail/{storeId}", "Detalle de la tienda", Icons.Filled.LocationOn, Icons.Outlined.LocationOn) {
        fun createRoute(storeId: Long) = "store-detail/$storeId"
    }

    data object Search : Screen(
        route = "search",
        title = "Buscar",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )

    data object Map : Screen(
        route = "map",
        title = "Mapa",
        selectedIcon = Icons.Filled.LocationOn,
        unselectedIcon = Icons.Outlined.LocationOn
    )

    data object Alerts : Screen(
        route = "alerts",
        title = "Alertas",
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications
    )

    data object Profile : Screen(
        route = "profile",
        title = "Perfil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    data object Reservations : Screen(
        route = "reservations",
        title = "Mis Reservas",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    data object Favorites : Screen(
        route = "favorites",
        title = "Mis Favoritos",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    data object Login : Screen(
        route = "login",
        title = "Iniciar Sesión",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    data object Register : Screen(
        route = "register",
        title = "Registro",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    data object Verification : Screen(
        route = "verification/{destination}",
        title = "Verificación",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    ) {
        fun createRoute(destination: String = "correo") = "verification/$destination"
    }

    companion object {
        // Se calcula al consultar la barra y no durante la inicialización del companion.
        // Así se evita una lista con objetos aún no inicializados al arrancar la app.
        val bottomNavItems: List<Screen>
            get() = listOf(Home, Search, Map, Alerts, Profile)
    }
}
