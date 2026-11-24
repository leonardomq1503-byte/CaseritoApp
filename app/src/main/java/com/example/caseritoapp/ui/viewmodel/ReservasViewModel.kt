package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.dao.ReservaConDetalle
import com.example.caseritoapp.data.repository.AuthRepository
import com.example.caseritoapp.data.repository.ReservaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ReservasUiState(
    val reservas: List<ReservaConDetalle> = emptyList(),
    val sincronizando: Boolean = false,
    val mensaje: String? = null,
    val autenticado: Boolean = false,
    val reservaCancelando: Long? = null,
    val mensajeAccion: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ReservasViewModel(
    private val authRepository: AuthRepository,
    private val reservaRepository: ReservaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReservasUiState(sincronizando = true))
    val uiState: StateFlow<ReservasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.localIdentity.collectLatest { identity ->
                if (identity == null) {
                    _uiState.value = ReservasUiState(autenticado = false)
                    return@collectLatest
                }

                _uiState.value = ReservasUiState(sincronizando = true, autenticado = true)
                var errorSync: String? = null
                try {
                    reservaRepository.sincronizarReservas(identity.authUserId, identity.localUserId)
                } catch (error: Exception) {
                    errorSync = error.message ?: "No se pudieron actualizar tus reservas."
                }
                _uiState.value = _uiState.value.copy(sincronizando = false, mensaje = errorSync)

                reservaRepository.observarReservas(identity.localUserId).collect { reservas ->
                    _uiState.value = _uiState.value.copy(reservas = reservas, sincronizando = false)
                }
            }
        }
    }

    fun sincronizarAhora() {
        viewModelScope.launch {
            val identity = authRepository.getOrSyncCurrentIdentity() ?: return@launch
            _uiState.value = _uiState.value.copy(sincronizando = true, mensaje = null)
            try {
                reservaRepository.sincronizarReservas(identity.authUserId, identity.localUserId)
                _uiState.value = _uiState.value.copy(sincronizando = false)
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    sincronizando = false,
                    mensaje = error.message ?: "No se pudieron actualizar tus reservas."
                )
            }
        }
    }

    fun cancelarReserva(idRemoto: Long) {
        if (_uiState.value.reservaCancelando != null) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(reservaCancelando = idRemoto, mensajeAccion = null)
            runCatching { reservaRepository.cancelarReserva(idRemoto) }
                .onSuccess { stock ->
                    _uiState.value = _uiState.value.copy(
                        mensajeAccion = "Reserva cancelada. Se devolvieron $stock unidad(es) al stock."
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        mensajeAccion = error.message?.takeIf { it.isNotBlank() }
                            ?: "No se pudo cancelar la reserva. Comprueba tu conexión e inténtalo nuevamente."
                    )
                }
            _uiState.value = _uiState.value.copy(reservaCancelando = null)
        }
    }

    fun limpiarMensajeAccion() {
        _uiState.value = _uiState.value.copy(mensajeAccion = null)
    }
}
