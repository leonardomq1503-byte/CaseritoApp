package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.remote.dto.MerchantLocationDto
import com.example.caseritoapp.data.remote.dto.MerchantOfferDto
import com.example.caseritoapp.data.remote.dto.MerchantStoreDto
import com.example.caseritoapp.data.repository.AuthRepository
import com.example.caseritoapp.data.repository.MerchantRepository
import com.example.caseritoapp.data.repository.NuevaOferta
import com.example.caseritoapp.data.repository.OfertaRepository
import com.example.caseritoapp.data.repository.TiendaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MerchantUiState(
    val tiendas: List<MerchantStoreDto> = emptyList(),
    val ubicaciones: List<MerchantLocationDto> = emptyList(),
    val ofertas: List<MerchantOfferDto> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)

class MerchantViewModel(
    private val merchantRepository: MerchantRepository,
    private val authRepository: AuthRepository,
    private val ofertaRepository: OfertaRepository,
    private val tiendaRepository: TiendaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MerchantUiState())
    val uiState = _uiState.asStateFlow()

    init { refrescar() }

    fun refrescar() {
        if (_uiState.value.cargando) return
        viewModelScope.launch {
            if (authRepository.getCurrentUserInfo() == null) {
                _uiState.value = MerchantUiState(error = "Inicia sesión para administrar tu comercio.")
                return@launch
            }
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            runCatching {
                val tiendas = merchantRepository.misTiendas()
                val sucursales = if (tiendas.any { it.estado == "verificada" }) merchantRepository.misUbicaciones() else emptyList()
                val ofertas = if (sucursales.isNotEmpty()) merchantRepository.misOfertas() else emptyList()
                Triple(tiendas, sucursales, ofertas)
            }.onSuccess { (tiendas, sucursales, ofertas) ->
                _uiState.value = _uiState.value.copy(tiendas = tiendas, ubicaciones = sucursales, ofertas = ofertas, cargando = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = error.message?.takeIf { it.isNotBlank() } ?: "No se pudo cargar el panel del comercio."
                )
            }
        }
    }

    fun crearOferta(datos: NuevaOferta) {
        if (_uiState.value.guardando) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true, error = null, mensaje = null)
            runCatching { merchantRepository.crearOferta(datos) }
                .onSuccess {
                    val syncTiendas = tiendaRepository.syncTiendas()
                    val syncOfertas = ofertaRepository.syncOfertas()
                    val catalogoActualizado = syncTiendas.isSuccess && syncOfertas.isSuccess
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        mensaje = if (catalogoActualizado) "Oferta publicada." else "Oferta publicada. El catálogo local se actualizará cuando vuelva la conexión."
                    )
                    refrescar()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(guardando = false, error = error.message ?: "No se pudo publicar la oferta.")
                }
        }
    }

    fun cambiarEstadoOferta(offer: MerchantOfferDto) {
        if (_uiState.value.guardando) return
        val nuevoEstado = if (offer.estado == "activo") "pausado" else "activo"
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true, error = null, mensaje = null)
            runCatching { merchantRepository.cambiarEstadoOferta(offer.id, nuevoEstado) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(guardando = false, mensaje = if (nuevoEstado == "pausado") "Oferta pausada." else "Oferta reactivada.")
                    refrescar()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(guardando = false, error = error.message ?: "No se pudo actualizar la oferta.")
                }
        }
    }

    fun limpiarMensajes() { _uiState.value = _uiState.value.copy(mensaje = null, error = null) }
}
