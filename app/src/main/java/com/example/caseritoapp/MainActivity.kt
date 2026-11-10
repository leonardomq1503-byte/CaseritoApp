package com.example.caseritoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.caseritoapp.navigation.AppNavigation
import com.example.caseritoapp.navigation.CaseritoBottomBar
import com.example.caseritoapp.navigation.Screen
import com.example.caseritoapp.ui.theme.CaseritoAppTheme
import com.example.caseritoapp.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.crearCanalNotificaciones(this)
        enableEdgeToEdge()
        setContent {
            CaseritoAppTheme {
                CaseritoMainApp()
            }
        }
    }
}

@Composable
fun CaseritoMainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val application = LocalContext.current.applicationContext as CaseritoApplication
    val alertasNoLeidas by application.notificacionRepository.getCantidadNoLeidas(1L)
        .collectAsStateWithLifecycle(initialValue = 0)

    // Ocultar barra inferior cuando estamos en pantallas de autenticación o flujos modales
    val showBottomBar = Screen.bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            if (showBottomBar) {
                CaseritoBottomBar(
                    navController = navController,
                    alertasNoLeidas = alertasNoLeidas
                )
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            paddingValues = innerPadding
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CaseritoMainAppPreview() {
    CaseritoAppTheme {
        CaseritoMainApp()
    }
}
