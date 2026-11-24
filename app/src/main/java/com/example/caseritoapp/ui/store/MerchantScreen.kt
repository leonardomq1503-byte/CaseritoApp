package com.example.caseritoapp.ui.store

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.caseritoapp.CaseritoApplication
import com.example.caseritoapp.data.repository.NuevaOferta
import com.example.caseritoapp.ui.viewmodel.CaseritoViewModelFactory
import com.example.caseritoapp.ui.viewmodel.MerchantViewModel
import java.time.LocalDate

@Composable
fun MerchantScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRegistration: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MerchantViewModel = viewModel(
        factory = CaseritoViewModelFactory(LocalContext.current.applicationContext as CaseritoApplication)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var locationId by remember { mutableStateOf<Long?>(null) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioRegular by remember { mutableStateOf("") }
    var precioOferta by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("1") }
    var fechaFin by remember { mutableStateOf("") }
    var errorFormulario by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.mensaje) {
        if (state.mensaje?.startsWith("Oferta publicada") == true) {
            nombre = ""; descripcion = ""; precioRegular = ""; precioOferta = ""; stock = "1"; fechaFin = ""
            mostrarFormulario = false
            errorFormulario = null
        }
    }

    LaunchedEffect(state.ubicaciones) {
        if (locationId == null) locationId = state.ubicaciones.firstOrNull()?.id
    }
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refrescar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver") }
            Text("Panel de mi tienda", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            IconButton(onClick = viewModel::refrescar, enabled = !state.cargando) { Icon(Icons.Outlined.Refresh, contentDescription = "Actualizar panel") }
        }

        if (state.cargando && state.tiendas.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp))
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp)) }
        state.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp)) }

        if (state.tiendas.isEmpty() && !state.cargando) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Registra tu negocio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Envía la información de tu tienda y su primera sucursal. Podrás publicar ofertas cuando la solicitud sea verificada.")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onNavigateToRegistration) { Text("Registrar tienda") }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.tiendas, key = { it.id }) { store ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(store.nombreComercial, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Solicitud: ${store.estado.estadoComerciante()}", style = MaterialTheme.typography.bodyMedium,
                                color = if (store.estado == "verificada") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
                            store.fechaRegistro?.takeIf { it.isNotBlank() }?.let { Text("Registrada: ${it.take(10)}", style = MaterialTheme.typography.bodySmall) }
                            if (store.estado != "verificada") {
                                Text(
                                    if (store.estado == "rechazada") "Revisa la información con soporte o envía una nueva solicitud."
                                    else "La publicación de ofertas estará disponible al aprobarse la tienda.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedButton(onClick = onNavigateToRegistration, modifier = Modifier.fillMaxWidth()) { Text("Registrar otra tienda") }
                }

                if (state.tiendas.none { it.estado == "verificada" }) {
                    item { Text("Las ofertas estarán disponibles cuando se verifique una de tus tiendas.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    item {
                        Text("Sucursales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.ubicaciones.filter { loc -> state.tiendas.any { it.id == loc.idTienda && it.estado == "verificada" } }.forEach { loc ->
                                FilterChip(selected = locationId == loc.id, onClick = { locationId = loc.id }, label = { Text(loc.nombreSucursal) })
                            }
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Ofertas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { mostrarFormulario = !mostrarFormulario }, enabled = !state.guardando) {
                                Icon(Icons.Outlined.Add, contentDescription = null)
                                Text(if (mostrarFormulario) "Cerrar" else "Nueva oferta")
                            }
                        }
                    }
                    if (mostrarFormulario) {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Publicar oferta", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    CampoOferta("Nombre del producto", nombre, { nombre = it })
                                    CampoOferta("Descripción (opcional)", descripcion, { descripcion = it })
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CampoOferta("Precio regular (S/)", precioRegular, { precioRegular = it }, KeyboardType.Decimal, Modifier.weight(1f))
                                        CampoOferta("Precio oferta (S/)", precioOferta, { precioOferta = it }, KeyboardType.Decimal, Modifier.weight(1f))
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CampoOferta("Stock", stock, { stock = it }, KeyboardType.Number, Modifier.weight(1f))
                                        CampoOferta("Fecha fin (AAAA-MM-DD)", fechaFin, { fechaFin = it }, KeyboardType.Text, Modifier.weight(1.4f))
                                    }
                                    errorFormulario?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                                    Button(
                                        onClick = {
                                            val regular = precioRegular.replace(',', '.').toDoubleOrNull()
                                            val promo = precioOferta.replace(',', '.').toDoubleOrNull()
                                            val unidades = stock.toIntOrNull()
                                            val fecha = runCatching { LocalDate.parse(fechaFin).atTime(23, 59, 59).toString() + "-05:00" }.getOrNull()
                                            errorFormulario = when {
                                                locationId == null -> "Selecciona una sucursal activa."
                                                nombre.isBlank() -> "Escribe el nombre del producto."
                                                regular == null || regular <= 0 -> "Ingresa un precio regular válido."
                                                promo == null || promo <= 0 || promo >= regular -> "El precio de oferta debe ser menor que el precio regular."
                                                unidades == null || unidades < 0 -> "Ingresa un stock válido."
                                                fecha == null || runCatching { LocalDate.parse(fechaFin).isAfter(LocalDate.now()) }.getOrDefault(false).not() -> "La fecha debe tener el formato AAAA-MM-DD y ser futura."
                                                else -> null
                                            }
                                            if (errorFormulario == null) {
                                                viewModel.crearOferta(NuevaOferta(locationId!!, nombre.trim(), descripcion.trim().ifBlank { null }, regular!!, promo!!, unidades!!, fecha!!))
                                            }
                                        },
                                        enabled = !state.guardando && locationId != null,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (state.guardando) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp) else Text("Publicar oferta")
                                    }
                                }
                            }
                        }
                    }

                    val offersInLocation = state.ofertas.filter { it.idUbicacion == locationId }
                    if (offersInLocation.isEmpty()) {
                        item { Text("Esta sucursal todavía no tiene ofertas.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) }
                    }
                    items(offersInLocation, key = { it.id }) { offer ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text(offer.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("S/ %.2f  ·  Stock: %d  ·  %s".format(offer.precioOferta, offer.stockDisponible, offer.estado.estadoComerciante()), style = MaterialTheme.typography.bodySmall)
                                Text("Vence: ${offer.fechaFin.take(10)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (offer.estado == "activo" || offer.estado == "pausado") {
                                    TextButtonMerchant(
                                        text = if (offer.estado == "activo") "Pausar oferta" else "Reactivar oferta",
                                        enabled = !state.guardando && offer.stockDisponible > 0,
                                        onClick = { viewModel.cambiarEstadoOferta(offer) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoOferta(etiqueta: String, valor: String, onChange: (String) -> Unit, teclado: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier) {
    OutlinedTextField(value = valor, onValueChange = onChange, label = { Text(etiqueta) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado), modifier = modifier.fillMaxWidth())
}

@Composable
private fun TextButtonMerchant(text: String, enabled: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick, enabled = enabled) { Text(text) }
}

private fun String.estadoComerciante(): String = when (lowercase()) {
    "pendiente" -> "Pendiente de revisión"
    "verificada" -> "Verificada"
    "rechazada" -> "Rechazada"
    "suspendida" -> "Suspendida"
    "activo" -> "Activa"
    "pausado" -> "Pausada"
    "agotado" -> "Agotada"
    "expirado" -> "Expirada"
    else -> replaceFirstChar { it.uppercase() }
}
