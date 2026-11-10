package com.example.caseritoapp.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.caseritoapp.CaseritoApplication
import com.example.caseritoapp.ui.components.CategoriaChip
import com.example.caseritoapp.ui.components.TiendaCard
import com.example.caseritoapp.ui.viewmodel.CaseritoViewModelFactory
import com.example.caseritoapp.ui.viewmodel.MapViewModel
import com.example.caseritoapp.ui.viewmodel.TiendaConUbicacion

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(
        factory = CaseritoViewModelFactory(
            LocalContext.current.applicationContext as CaseritoApplication
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val radios = listOf(1, 2, 5)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Lienzo interactivo del mapa con cuadrícula urbana y radio de cobertura
        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        val radarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        val roadColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Círculos concéntricos de rango de búsqueda
            drawCircle(
                color = radarColor,
                radius = canvasWidth * 0.45f,
                center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f)
            )
            drawCircle(
                color = radarColor.copy(alpha = 0.05f),
                radius = canvasWidth * 0.70f,
                center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f)
            )

            // Vías principales estilizadas (Av. Larco, Benavides, Diagonal)
            drawLine(
                color = roadColor,
                start = Offset(0f, canvasHeight * 0.35f),
                end = Offset(canvasWidth, canvasHeight * 0.55f),
                strokeWidth = 6f
            )
            drawLine(
                color = roadColor,
                start = Offset(canvasWidth * 0.30f, 0f),
                end = Offset(canvasWidth * 0.65f, canvasHeight),
                strokeWidth = 8f
            )
            drawLine(
                color = roadColor,
                start = Offset(canvasWidth * 0.85f, 0f),
                end = Offset(canvasWidth * 0.15f, canvasHeight),
                strokeWidth = 5f
            )

            // Marcador de "Mi ubicación" en el centro
            drawCircle(
                color = Color(0xFF178B63),
                radius = 18f,
                center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f)
            )
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f)
            )
        }

        // Pines interactivos de tiendas sobre el mapa
        Box(modifier = Modifier.fillMaxSize()) {
            uiState.tiendasConUbicacion.forEachIndexed { index, item ->
                val esSeleccionada = uiState.tiendaSeleccionada?.tienda?.id == item.tienda.id

                // Posicionamiento visual aproximado de sucursales en Lima
                val offsetX = when (index) {
                    0 -> 0.35f // TecnoStore (Larco)
                    1 -> 0.62f // Moda & Estilo (Cantuarias)
                    2 -> 0.48f // Bazar Don Pepe (Benavides)
                    else -> 0.70f
                }
                val offsetY = when (index) {
                    0 -> 0.32f
                    1 -> 0.42f
                    2 -> 0.58f
                    else -> 0.25f
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(
                                start = (LocalContext.current.resources.displayMetrics.widthPixels * offsetX / LocalContext.current.resources.displayMetrics.density).dp,
                                top = (LocalContext.current.resources.displayMetrics.heightPixels * offsetY / LocalContext.current.resources.displayMetrics.density).dp
                            )
                            .clickable { viewModel.seleccionarTienda(item) }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (esSeleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = if (esSeleccionada) Color.White else MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Storefront,
                                        contentDescription = null,
                                        tint = if (esSeleccionada) Color.White else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = item.tienda.nombreComercial,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (esSeleccionada) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = if (esSeleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(if (esSeleccionada) 32.dp else 24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Panel flotante superior: zona y selector de radio
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Zona Comercial Activa",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Miraflores, Lima (${uiState.tiendasConUbicacion.size} tiendas cercanas)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Radio:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        radios.forEach { r ->
                            CategoriaChip(
                                nombre = "$r km",
                                seleccionado = uiState.radioKm == r,
                                onClick = { viewModel.cambiarRadio(r) }
                            )
                        }
                    }
                }
            }
        }

        // Botón flotante para centrar o navegar
        FloatingActionButton(
            onClick = {
                uiState.tiendaSeleccionada?.let {
                    viewModel.abrirEnGoogleMaps(context, it)
                }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 180.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Directions,
                contentDescription = "Ruta en Google Maps"
            )
        }

        // Tarjeta inferior de la tienda seleccionada con botón de navegación
        AnimatedVisibility(
            visible = uiState.tiendaSeleccionada != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            uiState.tiendaSeleccionada?.let { tiendaConUbi ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TiendaCard(
                            nombre = tiendaConUbi.tienda.nombreComercial,
                            categoria = tiendaConUbi.tienda.categoriaPrincipal ?: "Comercio Local",
                            direccion = "${tiendaConUbi.ubicacion.direccion}, ${tiendaConUbi.ubicacion.ciudad}",
                            distanciaAprox = "A ${tiendaConUbi.distanciaMetros} m de ti",
                            cantidadOfertasActivas = 3,
                            onClick = {}
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.abrirEnGoogleMaps(context, tiendaConUbi) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Directions,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cómo llegar con Google Maps",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
