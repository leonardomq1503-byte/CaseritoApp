package com.example.caseritoapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.entity.NotificacionEntity
import com.example.caseritoapp.data.repository.AuthRepository
import com.example.caseritoapp.data.repository.NotificacionRepository
import com.example.caseritoapp.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class AlertsUiState(
    val notificaciones: List<NotificacionEntity> = emptyList(),
    val cantidadNoLeidas: Int = 0,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModel(
    private val notificacionRepository: NotificacionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState(isLoading = true))
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.localUserId.flatMapLatest { userId ->
                userId?.let { notificacionRepository.getNotificacionesPorUsuario(it) }
                    ?: flowOf(emptyList())
            }.collect { lista ->
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
            authRepository.getOrSyncCurrentLocalUserId()?.let {
                notificacionRepository.marcarComoLeida(id, it)
            }
        }
    }

    fun marcarTodasComoLeidas() {
        viewModelScope.launch {
            authRepository.getOrSyncCurrentLocalUserId()?.let {
                notificacionRepository.marcarTodasComoLeidas(it)
            }
        }
    }

    fun enviarNotificacionPrueba(context: Context) {
        viewModelScope.launch {
            val userId = authRepository.getOrSyncCurrentLocalUserId() ?: return@launch
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
