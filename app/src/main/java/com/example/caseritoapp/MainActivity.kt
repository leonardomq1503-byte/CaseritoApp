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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.caseritoapp.navigation.AppNavigation
import com.example.caseritoapp.navigation.CaseritoBottomBar
import com.example.caseritoapp.ui.theme.CaseritoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            CaseritoBottomBar(navController = navController)
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
