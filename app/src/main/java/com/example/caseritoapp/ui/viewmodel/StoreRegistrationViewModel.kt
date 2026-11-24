package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.repository.RegistroTienda
import com.example.caseritoapp.data.repository.TiendaRepository
import com.example.caseritoapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StoreRegistrationUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registered: Boolean = false
)

class StoreRegistrationViewModel(
    private val tiendaRepository: TiendaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoreRegistrationUiState())
    val uiState = _uiState.asStateFlow()

    fun registrar(datos: RegistroTienda) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = StoreRegistrationUiState(isLoading = true)
            runCatching { tiendaRepository.registrarTienda(datos) }
                .onSuccess {
                    runCatching { authRepository.actualizarRolComercianteLocal() }
                    _uiState.value = StoreRegistrationUiState(registered = true)
                }
                .onFailure { error ->
                    _uiState.value = StoreRegistrationUiState(
                        error = error.message ?: "No se pudo registrar el comercio. Intenta nuevamente."
                    )
                }
        }
    }
}
