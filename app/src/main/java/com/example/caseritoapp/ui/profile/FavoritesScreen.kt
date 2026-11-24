package com.example.caseritoapp.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.caseritoapp.CaseritoApplication
import com.example.caseritoapp.ui.components.OfertaCard
import com.example.caseritoapp.ui.components.TiendaCard
import com.example.caseritoapp.ui.viewmodel.CaseritoViewModelFactory
import com.example.caseritoapp.ui.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (Long) -> Unit,
    onNavigateToStore: (Long) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: FavoritesViewModel = viewModel(
        factory = CaseritoViewModelFactory(LocalContext.current.applicationContext as CaseritoApplication)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showProducts by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
            }
            Text("Mis Favoritos", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = showProducts, onClick = { showProducts = true }, label = { Text("Productos (${state.products.size})") })
            FilterChip(selected = !showProducts, onClick = { showProducts = false }, label = { Text("Tiendas (${state.stores.size})") })
        }
        Spacer(Modifier.height(8.dp))
        if (!state.authenticated) {
            Text("Inicia sesión para ver los favoritos guardados en tu cuenta.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            androidx.compose.material3.TextButton(onClick = onNavigateToLogin) { Text("Iniciar sesión") }
        } else if (showProducts && state.products.isEmpty()) {
            Text("Todavía no guardaste ofertas favoritas.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 20.dp))
        } else if (!showProducts && state.stores.isEmpty()) {
            Text("Todavía no guardaste tiendas favoritas.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 20.dp))
        } else if (showProducts) {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.products, key = { it.oferta.id }) { item ->
                    val offer = item.oferta
                    val discount = if (offer.precioOriginal > 0) ((offer.precioOriginal - offer.precioOferta) / offer.precioOriginal * 100).toInt() else 0
                    OfertaCard(
                        nombre = offer.nombre,
                        tienda = item.nombreTienda ?: item.nombreSucursal ?: "Comercio local",
                        precioOriginal = offer.precioOriginal,
                        precioOferta = offer.precioOferta,
                        porcentajeDescuento = discount,
                        esFavorito = true,
                        onFavoritoClick = { viewModel.quitarProducto(offer.id) },
                        mostrarReservar = false,
                        stockRestante = offer.stockDisponible,
                        onClick = { onNavigateToProduct(offer.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.stores, key = { it.id }) { store ->
                    TiendaCard(
                        nombre = store.nombreComercial,
                        categoria = store.categoriaPrincipal ?: "Comercio local",
                        distanciaAprox = "Ver sucursales",
                        direccion = store.razonSocial,
                        esFavorita = true,
                        onFavoritoClick = { viewModel.quitarTienda(store.id) },
                        onClick = { onNavigateToStore(store.id) }
                    )
                }
            }
        }
    }
}
