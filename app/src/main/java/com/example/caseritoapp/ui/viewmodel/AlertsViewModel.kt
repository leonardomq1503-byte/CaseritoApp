package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.entity.NotificacionEntity
import com.example.caseritoapp.data.repository.AuthRepository
import com.example.caseritoapp.data.repository.NotificacionRepository
import com.example.caseritoapp.data.repository.PersonalDataSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class AlertsUiState(
    val notificaciones: List<NotificacionEntity> = emptyList(),
    val cantidadNoLeidas: Int = 0,
    val isLoading: Boolean = false,
    val autenticado: Boolean = false,
    val sincronizando: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModel(
    private val notificacionRepository: NotificacionRepository,
    private val authRepository: AuthRepository,
    private val personalDataSyncRepository: PersonalDataSyncRepository
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
        viewModelScope.launch {
            authRepository.localIdentity.collectLatest { identity ->
                if (identity == null) {
                    _uiState.value = _uiState.value.copy(autenticado = false, sincronizando = false)
                } else {
                    _uiState.value = _uiState.value.copy(autenticado = true)
                    sincronizar(identity.authUserId, identity.localUserId)
                }
            }
        }
    }

    fun marcarComoLeida(id: Long) {
        viewModelScope.launch {
            val identity = authRepository.getOrSyncCurrentIdentity() ?: return@launch
            notificacionRepository.marcarComoLeida(id, identity.localUserId, identity.authUserId)
        }
    }

    fun marcarTodasComoLeidas() {
        viewModelScope.launch {
            val identity = authRepository.getOrSyncCurrentIdentity() ?: return@launch
            notificacionRepository.marcarTodasComoLeidas(identity.localUserId, identity.authUserId)
        }
    }

    fun sincronizarAhora() {
        viewModelScope.launch {
            val identity = authRepository.getOrSyncCurrentIdentity() ?: return@launch
            sincronizar(identity.authUserId, identity.localUserId)
        }
    }

    private suspend fun sincronizar(authUserId: String, localUserId: Long) {
        if (_uiState.value.sincronizando) return
        _uiState.value = _uiState.value.copy(sincronizando = true, error = null)
        runCatching { personalDataSyncRepository.sincronizar(authUserId, localUserId) }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message ?: "No se pudieron actualizar las alertas.")
            }
        _uiState.value = _uiState.value.copy(sincronizando = false)
    }
}
