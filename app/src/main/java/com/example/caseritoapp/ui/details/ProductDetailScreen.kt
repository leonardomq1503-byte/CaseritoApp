package com.example.caseritoapp.ui.details

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.caseritoapp.CaseritoApplication
import com.example.caseritoapp.ui.viewmodel.CaseritoViewModelFactory
import com.example.caseritoapp.ui.viewmodel.ProductDetailViewModel
import java.util.Locale

@Composable
fun ProductDetailScreen(
    productId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToStore: (Long) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = viewModel(
        factory = CaseritoViewModelFactory(LocalContext.current.applicationContext as CaseritoApplication)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(productId) { viewModel.cargar(productId) }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
            }
            Text("Detalle del producto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f))
            IconButton(onClick = {
                if (state.estaAutenticado) viewModel.alternarFavorito() else onNavigateToLogin()
            }) {
                Icon(
                    if (state.esFavorito) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (state.esFavorito) "Quitar de favoritos" else "Guardar en favoritos",
                    tint = if (state.esFavorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state.cargando) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.oferta == null) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No encontramos esta oferta en los datos guardados del dispositivo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val item = state.oferta!!
            val oferta = item.oferta
            val descuento = if (oferta.precioOriginal > 0) {
                (((oferta.precioOriginal - oferta.precioOferta) / oferta.precioOriginal) * 100).toInt().coerceAtLeast(0)
            } else 0
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(235.dp).clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f), modifier = Modifier.size(92.dp))
                    if (descuento > 0) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                            shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary
                        ) { Text("-$descuento%", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) }
                    }
                }
                if (state.imagenes.size > 1) {
                    Text("${state.imagenes.size} imágenes del producto", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 7.dp))
                }

                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Tag(if (oferta.estado == "activo") "OFERTA ACTIVA" else oferta.estado.uppercase(), MaterialTheme.colorScheme.primary)
                    item.nombreCategoria?.let { Tag(it, MaterialTheme.colorScheme.tertiary) }
                }
                Spacer(Modifier.height(10.dp))
                Text(oferta.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(item.nombreTienda ?: item.nombreSucursal ?: "Comercio local", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
                oferta.descripcion?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
                }
                Spacer(Modifier.height(15.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(moneda(oferta.precioOferta), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            if (oferta.precioOriginal > oferta.precioOferta) Text(moneda(oferta.precioOriginal), style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        DetailLine("Stock disponible", if (oferta.stockDisponible > 0) "${oferta.stockDisponible} unidades" else "Agotado")
                        DetailLine("Vigencia", "Hasta ${fechaCorta(oferta.fechaFin)}")
                        oferta.condiciones?.takeIf { it.isNotBlank() }?.let { DetailLine("Condiciones", it) }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Card(
                    onClick = { item.idTienda?.let(onNavigateToStore) },
                    enabled = item.idTienda != null,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(item.nombreTienda ?: "Tienda", fontWeight = FontWeight.Bold)
                            Text(listOfNotNull(item.nombreSucursal, item.direccion, item.ciudad).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            item.horarios?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        Icon(Icons.Outlined.LocationOn, contentDescription = "Ver tienda", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 10.dp)) }
                Spacer(Modifier.height(16.dp))
            }
            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 10.dp)) {
                state.mensajeReserva?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(bottom = 8.dp)) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onNavigateToMap,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Icon(Icons.Outlined.LocationOn, contentDescription = null); Text("Mapa", modifier = Modifier.padding(start = 5.dp)) }
                    IconButton(onClick = {
                        val text = "${oferta.nombre} por ${moneda(oferta.precioOferta)} en ${item.nombreTienda ?: "Caserito"}"
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Compartir oferta"))
                    }, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Icon(Icons.Outlined.Share, contentDescription = "Compartir oferta")
                    }
                    Button(
                        onClick = { if (state.estaAutenticado) viewModel.reservar() else onNavigateToLogin() },
                        enabled = oferta.estado == "activo" && oferta.stockDisponible > 0 && !state.reservando,
                        modifier = Modifier.weight(1.4f).height(50.dp), shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (state.reservando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Reservar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun Tag(texto: String, color: androidx.compose.ui.graphics.Color) {
    Surface(color = color, shape = RoundedCornerShape(7.dp)) {
        Text(texto, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.8f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1.2f))
    }
}

private fun moneda(precio: Double): String = "S/ " + String.format(Locale.forLanguageTag("es-PE"), "%.2f", precio)

private fun fechaCorta(fecha: String): String {
    val partes = fecha.substringBefore('T').split('-')
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else fecha
}
