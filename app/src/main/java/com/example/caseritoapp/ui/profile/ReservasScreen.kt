package com.example.caseritoapp.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.caseritoapp.CaseritoApplication
import com.example.caseritoapp.ui.viewmodel.CaseritoViewModelFactory
import com.example.caseritoapp.ui.viewmodel.ReservasViewModel

@Composable
fun ReservasScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ReservasViewModel = viewModel(
        factory = CaseritoViewModelFactory(
            LocalContext.current.applicationContext as CaseritoApplication
        )
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var reservaAConfirmar by remember { mutableStateOf<Long?>(null) }

    reservaAConfirmar?.let { idRemoto ->
        AlertDialog(
            onDismissRequest = { reservaAConfirmar = null },
            title = { Text("¿Cancelar reserva?") },
            text = { Text("La reserva quedará cancelada y las unidades volverán al stock de la tienda.") },
            confirmButton = {
                TextButton(onClick = {
                    reservaAConfirmar = null
                    viewModel.cancelarReserva(idRemoto)
                }) { Text("Sí, cancelar") }
            },
            dismissButton = { TextButton(onClick = { reservaAConfirmar = null }) { Text("Volver") } }
        )
    }

    state.mensajeAccion?.let { mensaje ->
        AlertDialog(
            onDismissRequest = viewModel::limpiarMensajeAccion,
            title = { Text(if (mensaje.startsWith("Reserva cancelada")) "Reserva cancelada" else "No se pudo completar") },
            text = { Text(mensaje) },
            confirmButton = { TextButton(onClick = viewModel::limpiarMensajeAccion) { Text("Aceptar") } }
        )
    }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    text = "Mis Reservas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            IconButton(onClick = viewModel::sincronizarAhora, enabled = !state.sincronizando) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Actualizar reservas")
            }
        }

        if (state.sincronizando) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
            }
        }

        state.mensaje?.let { mensaje ->
            Text(
                text = "No se pudo actualizar desde internet: $mensaje. Mostrando lo guardado en este dispositivo.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        when {
            !state.autenticado -> Column(modifier = Modifier.padding(top = 24.dp)) {
                Text("Inicia sesión para consultar tus reservas.", style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = onNavigateToLogin) { Text("Iniciar sesión") }
            }
            state.reservas.isEmpty() && !state.sincronizando -> Text(
                "Todavía no tienes reservas.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp)
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.reservas, key = { it.reserva.id }) { detalle ->
                    val reserva = detalle.reserva
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                detalle.nombreProducto,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Sucursal: ${detalle.nombreSucursal}", style = MaterialTheme.typography.bodyMedium)
                            Text("Cantidad: ${reserva.cantidad}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Estado: ${reserva.estado.estadoReservaLegible()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (reserva.estado == "cancelada" || reserva.estado == "expirada") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Fecha: ${reserva.fechaReserva.take(16).replace('T', ' ')}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            reserva.fechaExpiracion?.let { expiracion ->
                                Text(
                                    "Retira hasta: ${expiracion.take(16).replace('T', ' ')}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (reserva.estado == "pendiente" && reserva.idRemoto != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { reservaAConfirmar = reserva.idRemoto },
                                    enabled = state.reservaCancelando == null,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (state.reservaCancelando == reserva.idRemoto) {
                                        CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp)
                                    } else Text("Cancelar reserva")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.estadoReservaLegible(): String = when (this.lowercase()) {
    "pendiente" -> "Pendiente de confirmación"
    "confirmada" -> "Confirmada, lista para recoger"
    "recogida" -> "Recogida"
    "cancelada" -> "Cancelada"
    "expirada" -> "Expirada"
    else -> replaceFirstChar { it.uppercase() }
}
