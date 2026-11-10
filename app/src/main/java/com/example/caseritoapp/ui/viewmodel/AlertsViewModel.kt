package com.example.caseritoapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.entity.NotificacionEntity
import com.example.caseritoapp.data.repository.FavoritoRepository
import com.example.caseritoapp.data.repository.NotificacionRepository
import com.example.caseritoapp.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlertsUiState(
    val notificaciones: List<NotificacionEntity> = emptyList(),
    val cantidadNoLeidas: Int = 0,
    val isLoading: Boolean = false
)

class AlertsViewModel(
    private val notificacionRepository: NotificacionRepository,
    private val favoritoRepository: FavoritoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState(isLoading = true))
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    private var userId: Long = 1L

    init {
        viewModelScope.launch {
            userId = favoritoRepository.getOrCreateDefaultUserId()
            notificacionRepository.inicializarNotificacionesSiVacio(userId)

            notificacionRepository.getNotificacionesPorUsuario(userId).collect { lista ->
                _uiState.value = _uiState.value.copy(
                    notificaciones = lista,
                    cantidadNoLeidas = lista.count { !it.leida },
                    isLoading = false
                )
            }
        }
    }

    fun marcarComoLeida(id: Long) {
        viewModelScope.launch {
            notificacionRepository.marcarComoLeida(id, userId)
        }
    }

    fun marcarTodasComoLeidas() {
        viewModelScope.launch {
            notificacionRepository.marcarTodasComoLeidas(userId)
        }
    }

    fun enviarNotificacionPrueba(context: Context) {
        viewModelScope.launch {
            val titulo = "¡Oferta Flash en Miraflores! ⚡"
            val mensaje = "50% de descuento en la segunda prenda en Moda & Estilo solo por hoy."

            notificacionRepository.agregarNotificacion(
                idUsuario = userId,
                titulo = titulo,
                mensaje = mensaje,
                tipo = "nueva_oferta_tienda",
                idObjeto = 2L,
                tipoObjeto = "tienda"
            )

            NotificationHelper.mostrarNotificacion(
                context = context,
                titulo = titulo,
                mensaje = mensaje
            )
        }
    }
}
