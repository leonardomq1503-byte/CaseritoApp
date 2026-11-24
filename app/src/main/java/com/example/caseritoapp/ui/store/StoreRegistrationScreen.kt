package com.example.caseritoapp.ui.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.caseritoapp.CaseritoApplication
import com.example.caseritoapp.data.repository.RegistroTienda
import com.example.caseritoapp.ui.viewmodel.CaseritoViewModelFactory
import com.example.caseritoapp.ui.viewmodel.StoreRegistrationViewModel

@Composable
fun StoreRegistrationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StoreRegistrationViewModel = viewModel(
        factory = CaseritoViewModelFactory(LocalContext.current.applicationContext as CaseritoApplication)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var nombre by remember { mutableStateOf("") }
    var sucursal by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("Arequipa") }
    var razonSocial by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var horarios by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    var validacion by remember { mutableStateOf<String?>(null) }

    if (state.registered) {
        Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("Solicitud enviada", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Registramos tu comercio con estado pendiente de revisión. Te avisaremos cuando sea verificado.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) { Text("Volver al panel de mi tienda") }
        }
        return
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
            }
            Text("Registrar mi tienda", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, start = 8.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text("Cuéntanos sobre tu negocio y su primera sucursal. La solicitud quedará pendiente de verificación.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        Campo("Nombre comercial *", nombre, { nombre = it })
        Campo("Nombre de la sucursal *", sucursal, { sucursal = it })
        Campo("Dirección *", direccion, { direccion = it })
        Campo("Ciudad *", ciudad, { ciudad = it })
        Campo("Teléfono de la sucursal", telefono, { telefono = it }, KeyboardType.Phone)
        Campo("Categoría (ej. Bodega, Moda)", categoria, { categoria = it })
        Campo("Razón social (opcional)", razonSocial, { razonSocial = it })
        Campo("Horario de atención (opcional)", horarios, { horarios = it })
        Text("Ubicación en el mapa (opcional)", style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        Text("Ingresa ambas coordenadas o deja ambas vacías. Puedes agregarlas después.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Campo("Latitud", latitud, { latitud = it }, KeyboardType.Decimal, Modifier.weight(1f))
            Campo("Longitud", longitud, { longitud = it }, KeyboardType.Decimal, Modifier.weight(1f))
        }

        (validacion ?: state.error)?.let { mensaje ->
            Text(mensaje, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                val lat = latitud.replace(',', '.').toDoubleOrNull()
                val lon = longitud.replace(',', '.').toDoubleOrNull()
                validacion = when {
                    nombre.isBlank() || sucursal.isBlank() || direccion.isBlank() || ciudad.isBlank() -> "Completa los campos marcados con *"
                    latitud.isNotBlank() && lat == null -> "La latitud no es válida"
                    longitud.isNotBlank() && lon == null -> "La longitud no es válida"
                    (latitud.isBlank()) != (longitud.isBlank()) -> "Ingresa latitud y longitud juntas, o deja ambas vacías"
                    lat != null && lat !in -90.0..90.0 -> "La latitud debe estar entre -90 y 90"
                    lon != null && lon !in -180.0..180.0 -> "La longitud debe estar entre -180 y 180"
                    else -> null
                }
                if (validacion == null) viewModel.registrar(
                    RegistroTienda(
                        nombreComercial = nombre.trim(), nombreSucursal = sucursal.trim(),
                        direccion = direccion.trim(), ciudad = ciudad.trim(),
                        razonSocial = razonSocial.trim().ifBlank { null },
                        categoriaPrincipal = categoria.trim().ifBlank { null },
                        telefono = telefono.trim().ifBlank { null }, horarios = horarios.trim().ifBlank { null },
                        latitud = lat, longitud = lon
                    )
                )
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (state.isLoading) CircularProgressIndicator()
            else Text("Enviar solicitud", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Campo(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    teclado: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor, onValueChange = alCambiar, label = { Text(etiqueta) },
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        singleLine = true, modifier = modifier.fillMaxWidth().padding(vertical = 5.dp)
    )
}
