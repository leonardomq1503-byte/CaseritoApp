package com.example.caseritoapp.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.caseritoapp.CaseritoApplication
import com.example.caseritoapp.ui.components.BuscadorCaserito
import com.example.caseritoapp.ui.components.CategoriaChip
import com.example.caseritoapp.ui.components.OfertaCard
import com.example.caseritoapp.ui.components.TiendaCard
import com.example.caseritoapp.ui.viewmodel.CaseritoViewModelFactory
import com.example.caseritoapp.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(
        factory = CaseritoViewModelFactory(
            LocalContext.current.applicationContext as CaseritoApplication
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val idsFavoritos by viewModel.idsFavoritos.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    // Lista dinámica de nombres de categorías (comienza con "Todas")
    val categoriasNombres = remember(uiState.categorias) {
        val list = mutableListOf("Todas")
        list.addAll(uiState.categorias.map { it.nombre })
        list.distinct()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Cabecera de Ubicación y Notificaciones
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Ubicación",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = " Mi ubicación",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Miraflores, Lima",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Indicador de sincronización en segundo plano con Supabase
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 6.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onNavigateToAlerts) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Alertas",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Notificación de estado de sincronización si aplica
        if (uiState.mensajeError != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = uiState.mensajeError!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Buscador Caserito
        item {
            BuscadorCaserito(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = onNavigateToSearch,
                onFilterClick = onNavigateToSearch,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Chips de Categorías
        item {
            Spacer(modifier = Modifier.height(14.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoriasNombres) { cat ->
                    CategoriaChip(
                        nombre = cat,
                        seleccionado = uiState.categoriaSeleccionada == cat,
                        onClick = { viewModel.seleccionarCategoria(cat) }
                    )
                }
            }
        }

        // Sección: Ofertas Destacadas
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ofertas destacadas",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onNavigateToSearch) {
                    Text(
                        text = "Ver todas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.ofertas.isEmpty() && !uiState.isSyncing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay ofertas en esta categoría por ahora",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.ofertas, key = { it.oferta.id }) { item ->
                        val oferta = item.oferta
                        val porcentajeDesc = if (oferta.precioOriginal > 0) {
                            (((oferta.precioOriginal - oferta.precioOferta) / oferta.precioOriginal) * 100).toInt()
                        } else 0

                        val esFav = oferta.id in idsFavoritos

                        OfertaCard(
                            nombre = oferta.nombre,
                            tienda = item.nombreTienda ?: item.nombreSucursal ?: "Tienda Caserito",
                            precioOriginal = oferta.precioOriginal,
                            precioOferta = oferta.precioOferta,
                            porcentajeDescuento = porcentajeDesc,
                            esFavorito = esFav,
                            onFavoritoClick = { viewModel.toggleFavorito(oferta.id) },
                            stockRestante = oferta.stockDisponible,
                            onClick = {}
                        )
                    }
                }
            }
        }

        // Sección: Tiendas Cercanas
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tiendas cerca de ti",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onNavigateToMap) {
                    Text(
                        text = "Ver en mapa",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.tiendas.isEmpty() && !uiState.isSyncing) {
                    Text(
                        text = "No se encontraron tiendas verificadas cercanas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.tiendas.forEach { tienda ->
                        TiendaCard(
                            nombre = tienda.nombreComercial,
                            categoria = tienda.categoriaPrincipal ?: "Comercio Local",
                            direccion = tienda.razonSocial ?: "Lima, Perú",
                            distanciaAprox = "A pocos minutos",
                            cantidadOfertasActivas = 3,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}
