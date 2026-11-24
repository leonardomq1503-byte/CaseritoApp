package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.dao.OfertaConDetalle
import com.example.caseritoapp.data.local.entity.ImagenProductoEntity
import com.example.caseritoapp.data.repository.AuthRepository
import com.example.caseritoapp.data.repository.FavoritoRepository
import com.example.caseritoapp.data.repository.OfertaRepository
import com.example.caseritoapp.data.repository.ReservaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val oferta: OfertaConDetalle? = null,
    val imagenes: List<ImagenProductoEntity> = emptyList(),
    val esFavorito: Boolean = false,
    val estaAutenticado: Boolean = false,
    val cargando: Boolean = true,
    val reservando: Boolean = false,
    val mensajeReserva: String? = null,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModel(
    private val ofertaRepository: OfertaRepository,
    private val favoritoRepository: FavoritoRepository,
    private val authRepository: AuthRepository,
    private val reservaRepository: ReservaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState = _uiState.asStateFlow()
    private var observeJob: Job? = null
    private var favoriteJob: Job? = null

    fun cargar(idProducto: Long) {
        observeJob?.cancel()
        favoriteJob?.cancel()
        _uiState.value = ProductDetailUiState()
        observeJob = viewModelScope.launch {
            combine(
                ofertaRepository.observarOfertaConDetalle(idProducto),
                ofertaRepository.observarImagenes(idProducto)
            ) { oferta, imagenes -> oferta to imagenes }
                .collect { (oferta, imagenes) ->
                    _uiState.value = _uiState.value.copy(oferta = oferta, imagenes = imagenes, cargando = false)
                }
        }
        favoriteJob = viewModelScope.launch {
            authRepository.localUserId.flatMapLatest { userId ->
                if (userId == null) flowOf(false)
                else favoritoRepository.getIdsFavoritos(userId, "producto").map { idProducto in it }
            }.collect { favorito ->
                _uiState.value = _uiState.value.copy(esFavorito = favorito, estaAutenticado = authRepository.getCurrentUserInfo() != null)
            }
        }
        viewModelScope.launch {
            authRepository.localUserId.collect { userId ->
                _uiState.value = _uiState.value.copy(estaAutenticado = userId != null)
            }
        }
    }

    fun alternarFavorito() {
        viewModelScope.launch {
            val ofertaId = _uiState.value.oferta?.oferta?.id ?: return@launch
            val identity = authRepository.getOrSyncCurrentIdentity()
            if (identity == null) {
                _uiState.value = _uiState.value.copy(error = "Inicia sesión para guardar favoritos.")
                return@launch
            }
            runCatching {
                favoritoRepository.toggleFavorito(identity.localUserId, identity.authUserId, "producto", ofertaId)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message ?: "No se pudo actualizar el favorito.")
            }
        }
    }

    fun reservar() {
        val item = _uiState.value.oferta?.oferta ?: return
        if (_uiState.value.reservando) return
        viewModelScope.launch {
            val identity = authRepository.getOrSyncCurrentIdentity()
            if (identity == null) {
                _uiState.value = _uiState.value.copy(error = "Inicia sesión para reservar esta oferta.")
                return@launch
            }
            _uiState.value = _uiState.value.copy(reservando = true, mensajeReserva = null, error = null)
            runCatching {
                reservaRepository.crearReserva(identity.localUserId, item.id, item.idUbicacion, 1)
            }.onSuccess { reserva ->
                _uiState.value = _uiState.value.copy(
                    reservando = false,
                    mensajeReserva = "Reserva confirmada. Quedan ${reserva.stockRestante} unidades disponibles."
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    reservando = false,
                    error = error.message ?: "No se pudo reservar. Intenta nuevamente."
                )
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(mensajeReserva = null, error = null)
    }
}
