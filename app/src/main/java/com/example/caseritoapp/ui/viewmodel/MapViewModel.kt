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
    val distanciaMetros: Int
)

data class MapUiState(
    val tiendasConUbicacion: List<TiendaConUbicacion> = emptyList(),
    val tiendaSeleccionada: TiendaConUbicacion? = null,
    val latitudCentro: Double = MapViewModel.AREQUIPA_LATITUDE,
    val longitudCentro: Double = MapViewModel.AREQUIPA_LONGITUDE,
    val radioKm: Int = 2,
    val isLoading: Boolean = false
)

class MapViewModel(
    private val tiendaRepository: TiendaRepository
) : ViewModel() {

    companion object {
        const val AREQUIPA_LATITUDE = -16.3988
        const val AREQUIPA_LONGITUDE = -71.5369
    }

    private val _tiendaSeleccionada = MutableStateFlow<TiendaConUbicacion?>(null)
    private val _radioKm = MutableStateFlow(2)

    val uiState: StateFlow<MapUiState> = combine(
        tiendaRepository.tiendasVerificadas,
        tiendaRepository.ubicacionesActivas,
        _tiendaSeleccionada,
        _radioKm
    ) { tiendas, ubicaciones, seleccionada, radio ->
        val combinadas = ubicaciones.mapNotNull { ubi ->
            val latitud = ubi.latitud ?: return@mapNotNull null
            val longitud = ubi.longitud ?: return@mapNotNull null
            val tienda = tiendas.find { it.id == ubi.idTienda }
            if (tienda != null) {
                TiendaConUbicacion(
                    tienda = tienda,
                    ubicacion = ubi,
                    distanciaMetros = distanciaEnMetros(
                        AREQUIPA_LATITUDE,
                        AREQUIPA_LONGITUDE,
                        latitud,
                        longitud
                    )
                )
            } else null
        }

        val tiendaActiva = seleccionada?.let { seleccionada ->
            combinadas.firstOrNull { it.tienda.id == seleccionada.tienda.id }
        }

        MapUiState(
            tiendasConUbicacion = combinadas,
            tiendaSeleccionada = tiendaActiva,
            radioKm = radio,
            latitudCentro = tiendaActiva?.ubicacion?.latitud ?: AREQUIPA_LATITUDE,
            longitudCentro = tiendaActiva?.ubicacion?.longitud ?: AREQUIPA_LONGITUDE
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
        val lat = requireNotNull(tiendaConUbi.ubicacion.latitud)
        val lng = requireNotNull(tiendaConUbi.ubicacion.longitud)
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

    private fun distanciaEnMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val radioTierraMetros = 6_371_000.0
        val diferenciaLatitud = Math.toRadians(lat2 - lat1)
        val diferenciaLongitud = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(diferenciaLatitud / 2) * kotlin.math.sin(diferenciaLatitud / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(diferenciaLongitud / 2) * kotlin.math.sin(diferenciaLongitud / 2)
        return (2 * radioTierraMetros * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))).toInt()
    }
}
