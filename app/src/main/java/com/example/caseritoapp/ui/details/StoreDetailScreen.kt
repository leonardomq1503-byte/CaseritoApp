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
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.caseritoapp.CaseritoApplication
import com.example.caseritoapp.data.local.entity.UbicacionEntity
import com.example.caseritoapp.ui.viewmodel.CaseritoViewModelFactory
import com.example.caseritoapp.ui.viewmodel.StoreDetailViewModel

@Composable
fun StoreDetailScreen(
    storeId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StoreDetailViewModel = viewModel(
        factory = CaseritoViewModelFactory(LocalContext.current.applicationContext as CaseritoApplication)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(storeId) { viewModel.cargar(storeId) }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver") }
            Text("Detalle de la tienda", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (state.cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (state.tienda == null) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No encontramos esta tienda en los datos guardados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val tienda = state.tienda!!
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                        .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(92.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f), shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(tienda.categoriaPrincipal ?: tienda.tipoNegocio ?: "Comercio local", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp))
                    }
                }

                Column(Modifier.padding(horizontal = 18.dp)) {
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tienda.nombreComercial, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(listOfNotNull(tienda.categoriaPrincipal, tienda.tipoNegocio).distinct().joinToString(" · "),
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = {
                            val text = "${tienda.nombreComercial} en Caserito${tienda.descripcion?.let { ": $it" }.orEmpty()}"
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Compartir tienda"))
                        }) { Icon(Icons.Outlined.Share, contentDescription = "Compartir tienda") }
                    }
                    tienda.descripcion?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 9.dp))
                    }

                    Spacer(Modifier.height(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Sucursales (${state.ubicaciones.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (tienda.web != null) Text(tienda.web, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.height(8.dp))
                    if (state.ubicaciones.isEmpty()) {
                        Text("No hay sucursales activas registradas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        state.ubicaciones.forEach { branch ->
                            SucursalCard(
                                ubicacion = branch,
                                onMap = { abrirMapa(context, branch) },
                                onCall = { branch.telefono?.let { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(it)}"))) } }
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Text("Ofertas disponibles (${state.ofertas.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (state.ofertas.isEmpty()) {
                        Text("Esta tienda no tiene ofertas activas por ahora.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        state.ofertas.forEach { item ->
                            Card(
                                onClick = { onNavigateToProduct(item.oferta.id) },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Outlined.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                        Text(item.oferta.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(item.nombreSucursal ?: "Sucursal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("S/ %.2f".format(item.oferta.precioOferta), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        if (item.oferta.precioOriginal > item.oferta.precioOferta) Text("Antes S/ %.2f".format(item.oferta.precioOriginal), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
private fun SucursalCard(ubicacion: UbicacionEntity, onMap: () -> Unit, onCall: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(15.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(ubicacion.nombreSucursal, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("${ubicacion.direccion}, ${ubicacion.ciudad}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp))
            ubicacion.horarios?.takeIf { it.isNotBlank() }?.let {
                Text("Horario: $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
            HorizontalDivider(Modifier.padding(top = 10.dp, bottom = 5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (ubicacion.latitud != null && ubicacion.longitud != null) {
                    OutlinedButton(onClick = onMap, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(17.dp)); Text("Ver mapa", modifier = Modifier.padding(start = 4.dp))
                    }
                }
                if (!ubicacion.telefono.isNullOrBlank()) {
                    OutlinedButton(onClick = onCall, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(17.dp)); Text("Llamar", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    }
}

private fun abrirMapa(context: android.content.Context, branch: UbicacionEntity) {
    val lat = branch.latitud ?: return
    val lon = branch.longitud ?: return
    val name = Uri.encode("${branch.nombreSucursal}, ${branch.direccion}")
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lon?q=$lat,$lon($name)"))
    runCatching { context.startActivity(intent) }.onFailure {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")))
    }
}
