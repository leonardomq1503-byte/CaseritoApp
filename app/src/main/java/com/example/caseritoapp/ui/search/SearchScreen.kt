package com.example.caseritoapp.ui.search

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.caseritoapp.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(
        factory = CaseritoViewModelFactory(
            LocalContext.current.applicationContext as CaseritoApplication
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val categoriasNombres = remember(uiState.categorias) {
        val list = mutableListOf("Todas")
        list.addAll(uiState.categorias.map { it.nombre })
        list.distinct()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Título de sección
        Text(
            text = "Explorar y Buscar",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        // Buscador reactivo
        BuscadorCaserito(
            query = uiState.query,
            onQueryChange = { viewModel.onQueryChange(it) },
            placeholderText = "¿Qué oferta buscas hoy?",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Filtro de categorías reactivo
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categoriasNombres) { cat ->
                CategoriaChip(
                    nombre = cat,
                    seleccionado = uiState.categoriaSeleccionada == cat,
                    onClick = { viewModel.onCategoriaChange(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Conteo de resultados dinámico
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resultados encontrados",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${uiState.totalResultados} coincidencias",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Ofertas encontradas
            if (uiState.ofertas.isNotEmpty()) {
                item {
                    Text(
                        text = "Ofertas (${uiState.ofertas.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.ofertas, key = { it.oferta.id }) { item ->
                            val oferta = item.oferta
                            val porcentajeDesc = if (oferta.precioOriginal > 0) {
                                (((oferta.precioOriginal - oferta.precioOferta) / oferta.precioOriginal) * 100).toInt()
                            } else 0

                            OfertaCard(
                                nombre = oferta.nombre,
                                tienda = item.nombreTienda ?: item.nombreSucursal ?: "Tienda Caserito",
                                precioOriginal = oferta.precioOriginal,
                                precioOferta = oferta.precioOferta,
                                porcentajeDescuento = porcentajeDesc,
                                esFavorito = false,
                                onFavoritoClick = {},
                                stockRestante = oferta.stockDisponible,
                                onClick = {}
                            )
                        }
                    }
                }
            }

            // Tiendas encontradas
            if (uiState.tiendas.isNotEmpty()) {
                item {
                    Text(
                        text = "Tiendas (${uiState.tiendas.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }

                items(uiState.tiendas, key = { it.id }) { tienda ->
                    TiendaCard(
                        nombre = tienda.nombreComercial,
                        categoria = tienda.categoriaPrincipal ?: "Comercio Local",
                        direccion = tienda.razonSocial ?: "Lima, Perú",
                        distanciaAprox = "Cerca de tu zona",
                        cantidadOfertasActivas = 3,
                        onClick = {}
                    )
                }
            }

            if (uiState.ofertas.isEmpty() && uiState.tiendas.isEmpty()) {
                item {
                    Text(
                        text = "No se encontraron resultados para '${uiState.query}'. Intenta con otro término o categoría.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
            }
        }
    }
}
