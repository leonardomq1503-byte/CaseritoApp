package com.example.caseritoapp.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    onNavigateToStoreDetail: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(
        factory = CaseritoViewModelFactory(
            LocalContext.current.applicationContext as CaseritoApplication
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val radios = listOf(1, 2, 5)

    // Centro de Arequipa usado como vista inicial.
    val arequipaCenter = remember {
        LatLng(MapViewModel.AREQUIPA_LATITUDE, MapViewModel.AREQUIPA_LONGITUDE)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipaCenter, 13.5f)
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    }

    // Estilo Clean Caserito aplicado al mapa
    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false,
            mapStyleOptions = MapStyleOptions(CaseritoMapStyle.JSON_STYLE)
        )
    }

    // Centrado animado al seleccionar una tienda
    LaunchedEffect(uiState.tiendaSeleccionada) {
        uiState.tiendaSeleccionada?.let { tiendaConUbi ->
            val lat = requireNotNull(tiendaConUbi.ubicacion.latitud)
            val lng = requireNotNull(tiendaConUbi.ubicacion.longitud)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 16f),
                durationMs = 800
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Google Maps con diseño minimalista Caserito
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings
        ) {
            // Marcadores personalizados con la paleta y logo de Caserito
            uiState.tiendasConUbicacion.forEach { item ->
                val lat = requireNotNull(item.ubicacion.latitud)
                val lng = requireNotNull(item.ubicacion.longitud)
                val esSeleccionada = uiState.tiendaSeleccionada?.tienda?.id == item.tienda.id

                MarkerComposable(
                    state = MarkerState(position = LatLng(lat, lng)),
                    title = item.tienda.nombreComercial,
                    onClick = {
                        viewModel.seleccionarTienda(item)
                        true
                    }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (esSeleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (esSeleccionada) Color.White else MaterialTheme.colorScheme.primary
                                )
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Storefront,
                                    contentDescription = null,
                                    tint = if (esSeleccionada) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
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
                            tint = if (esSeleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            modifier = Modifier.size(if (esSeleccionada) 32.dp else 26.dp)
                        )
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
                                text = "Comercios Caserito",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Arequipa, Perú",
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

        // Botón flotante para re-centrar la vista en Arequipa
        FloatingActionButton(
            onClick = {
                scope.launch {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(arequipaCenter, 13.5f),
                        durationMs = 700
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 220.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.MyLocation,
                contentDescription = "Centrar en mi zona"
            )
        }

        // Tarjeta inferior de la tienda seleccionada con botón de navegación GPS
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
                            distanciaAprox = "A ${tiendaConUbi.distanciaMetros} m del centro de Arequipa",
                            onClick = { onNavigateToStoreDetail(tiendaConUbi.tienda.id) }
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
