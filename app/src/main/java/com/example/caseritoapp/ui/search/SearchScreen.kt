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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.caseritoapp.ui.components.BuscadorCaserito
import com.example.caseritoapp.ui.components.CategoriaChip
import com.example.caseritoapp.ui.components.TiendaCard

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todas") }
    val categorias = listOf("Todas", "Ropa", "Electrónica", "Hogar", "Deportes", "Comida")

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

        // Buscador
        BuscadorCaserito(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholderText = "¿Qué oferta buscas hoy?",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Filtro de categorías
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categorias) { cat ->
                CategoriaChip(
                    nombre = cat,
                    seleccionado = categoriaSeleccionada == cat,
                    onClick = { categoriaSeleccionada = cat }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resultados
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resultados cercanos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "3 tiendas encontradas",
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
            item {
                TiendaCard(
                    nombre = "TecnoStore",
                    categoria = "Electrónica",
                    direccion = "Av. Larco 450",
                    distanciaAprox = "A 350 m",
                    cantidadOfertasActivas = 8,
                    onClick = {}
                )
            }
            item {
                TiendaCard(
                    nombre = "Moda & Estilo",
                    categoria = "Ropa",
                    direccion = "Calle Cantuarias 120",
                    distanciaAprox = "A 600 m",
                    cantidadOfertasActivas = 14,
                    onClick = {}
                )
            }
            item {
                TiendaCard(
                    nombre = "Bazar Don Pepe",
                    categoria = "Hogar",
                    direccion = "Av. Benavides 1100",
                    distanciaAprox = "A 950 m",
                    cantidadOfertasActivas = 5,
                    onClick = {}
                )
            }
        }
    }
}
