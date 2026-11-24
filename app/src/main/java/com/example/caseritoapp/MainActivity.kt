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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.caseritoapp.navigation.AppNavigation
import com.example.caseritoapp.navigation.CaseritoBottomBar
import com.example.caseritoapp.navigation.Screen
import com.example.caseritoapp.data.sync.SyncScheduler
import com.example.caseritoapp.ui.theme.CaseritoAppTheme
import com.example.caseritoapp.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.crearCanalNotificaciones(this)
        enableEdgeToEdge()
        val openAlerts = intent.getBooleanExtra(EXTRA_OPEN_ALERTS, false)
        setContent {
            CaseritoAppTheme {
                CaseritoMainApp(openAlertsOnStart = openAlerts)
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_ALERTS = "com.example.caseritoapp.OPEN_ALERTS"
    }
}

@Composable
@OptIn(ExperimentalCoroutinesApi::class)
fun CaseritoMainApp(openAlertsOnStart: Boolean = false) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val application = LocalContext.current.applicationContext as CaseritoApplication
    val preferences = remember(application) {
        application.getSharedPreferences("caserito_preferences", android.content.Context.MODE_PRIVATE)
    }
    val startDestination = remember(application, openAlertsOnStart) {
        if (openAlertsOnStart) Screen.Alerts.route
        else if (preferences.getBoolean("welcome_seen", false) || application.authRepository.getCurrentUserInfo() != null) {
            Screen.Home.route
        } else Screen.Welcome.route
    }
    LaunchedEffect(application) {
        application.authRepository.localIdentity.collect { identity ->
            if (identity != null) {
                SyncScheduler.enqueue(application, identity.authUserId)
            } else {
                SyncScheduler.cancelPeriodic(application)
            }
        }
    }
    val alertasNoLeidas by application.authRepository.localUserId
        .flatMapLatest { userId ->
            userId?.let { application.notificacionRepository.getCantidadNoLeidas(it) }
                ?: flowOf(0)
        }
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
            paddingValues = innerPadding,
            startDestination = startDestination,
            onWelcomeFinished = {
                preferences.edit().putBoolean("welcome_seen", true).apply()
            }
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
