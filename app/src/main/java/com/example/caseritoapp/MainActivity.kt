package com.example.caseritoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.caseritoapp.ui.components.BuscadorCaserito
import com.example.caseritoapp.ui.components.CategoriaChip
import com.example.caseritoapp.ui.components.OfertaCard
import com.example.caseritoapp.ui.components.TiendaCard
import com.example.caseritoapp.ui.theme.CaseritoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CaseritoAppTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { innerPadding ->
                    Fase1ShowcaseScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Fase1ShowcaseScreen(modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todas") }
    var favoritoOferta1 by remember { mutableStateOf(false) }
    var favoritoOferta2 by remember { mutableStateOf(true) }

    val categorias = listOf("Todas", "Ropa", "Electrónica", "Hogar", "Deportes", "Comida")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Cabecera de Ubicación y Marca Caserito
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

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Alertas",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Buscador Caserito
        item {
            BuscadorCaserito(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onFilterClick = {},
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
                items(categorias) { cat ->
                    CategoriaChip(
                        nombre = cat,
                        seleccionado = categoriaSeleccionada == cat,
                        onClick = { categoriaSeleccionada = cat }
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
                Text(
                    text = "Ver todas",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    OfertaCard(
                        nombre = "Audífonos Bluetooth Pro",
                        tienda = "TecnoStore Miraflores",
                        precioOriginal = 159.00,
                        precioOferta = 89.00,
                        porcentajeDescuento = 44,
                        esFavorito = favoritoOferta1,
                        onFavoritoClick = { favoritoOferta1 = !favoritoOferta1 },
                        stockRestante = 3,
                        onClick = {}
                    )
                }
                item {
                    OfertaCard(
                        nombre = "Casaca Impermeable Urbana",
                        tienda = "Moda & Estilo",
                        precioOriginal = 220.00,
                        precioOferta = 132.00,
                        porcentajeDescuento = 40,
                        esFavorito = favoritoOferta2,
                        onFavoritoClick = { favoritoOferta2 = !favoritoOferta2 },
                        stockRestante = 8,
                        onClick = {}
                    )
                }
                item {
                    OfertaCard(
                        nombre = "Zapatillas Deportivas Runner",
                        tienda = "SportZone Perú",
                        precioOriginal = 299.00,
                        precioOferta = 179.40,
                        porcentajeDescuento = 40,
                        esFavorito = false,
                        onFavoritoClick = {},
                        stockRestante = 2,
                        onClick = {}
                    )
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
                Text(
                    text = "Ver en mapa",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TiendaCard(
                    nombre = "TecnoStore",
                    categoria = "Electrónica",
                    direccion = "Av. Larco 450",
                    distanciaAprox = "A 350 m",
                    cantidadOfertasActivas = 8,
                    onClick = {}
                )
                TiendaCard(
                    nombre = "Moda & Estilo",
                    categoria = "Ropa",
                    direccion = "Calle Cantuarias 120",
                    distanciaAprox = "A 600 m",
                    cantidadOfertasActivas = 14,
                    onClick = {}
                )
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Fase1ShowcasePreview() {
    CaseritoAppTheme {
        Fase1ShowcaseScreen()
    }
}
