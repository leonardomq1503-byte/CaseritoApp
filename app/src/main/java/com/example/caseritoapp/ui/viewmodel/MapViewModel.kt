package com.example.caseritoapp.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.entity.TiendaEntity
import com.example.caseritoapp.data.local.entity.UbicacionEntity
import com.example.caseritoapp.data.repository.TiendaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TiendaConUbicacion(
    val tienda: TiendaEntity,
    val ubicacion: UbicacionEntity,
    val distanciaMetros: Int = 350
)

data class MapUiState(
    val tiendasConUbicacion: List<TiendaConUbicacion> = emptyList(),
    val tiendaSeleccionada: TiendaConUbicacion? = null,
    val latitudCentro: Double = -12.1217,
    val longitudCentro: Double = -77.0298,
    val radioKm: Int = 2,
    val isLoading: Boolean = false
)

class MapViewModel(
    private val tiendaRepository: TiendaRepository
) : ViewModel() {

    private val _tiendaSeleccionada = MutableStateFlow<TiendaConUbicacion?>(null)
    private val _radioKm = MutableStateFlow(2)

    val uiState: StateFlow<MapUiState> = combine(
        tiendaRepository.tiendasVerificadas,
        tiendaRepository.ubicacionesActivas,
        _tiendaSeleccionada,
        _radioKm
    ) { tiendas, ubicaciones, seleccionada, radio ->
        val combinadas = ubicaciones.mapNotNull { ubi ->
            val tienda = tiendas.find { it.id == ubi.idTienda }
            if (tienda != null) {
                // Distancia aproximada ficticia basada en sucursal
                val distancia = when (tienda.id) {
                    1L -> 350
                    2L -> 600
                    3L -> 950
                    else -> 1200
                }
                TiendaConUbicacion(
                    tienda = tienda,
                    ubicacion = ubi,
                    distanciaMetros = distancia
                )
            } else null
        }

        val tiendaActiva = seleccionada ?: combinadas.firstOrNull()

        MapUiState(
            tiendasConUbicacion = combinadas,
            tiendaSeleccionada = tiendaActiva,
            radioKm = radio,
            latitudCentro = tiendaActiva?.ubicacion?.latitud ?: -12.1217,
            longitudCentro = tiendaActiva?.ubicacion?.longitud ?: -77.0298
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapUiState(isLoading = true)
    )

    fun seleccionarTienda(item: TiendaConUbicacion) {
        _tiendaSeleccionada.value = item
    }

    fun cambiarRadio(nuevoRadio: Int) {
        _radioKm.value = nuevoRadio
    }

    fun abrirEnGoogleMaps(context: Context, tiendaConUbi: TiendaConUbicacion) {
        val lat = tiendaConUbi.ubicacion.latitud ?: -12.1217
        val lng = tiendaConUbi.ubicacion.longitud ?: -77.0298
        val nombre = Uri.encode(tiendaConUbi.tienda.nombreComercial)

        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($nombre)")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Si no tiene app de mapas, abrir en navegador
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }
}
