package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.dao.OfertaConDetalle
import com.example.caseritoapp.data.local.entity.CategoriaEntity
import com.example.caseritoapp.data.local.entity.TiendaEntity
import com.example.caseritoapp.data.repository.FavoritoRepository
import com.example.caseritoapp.data.repository.AuthRepository
import com.example.caseritoapp.data.repository.OfertaRepository
import com.example.caseritoapp.data.repository.TiendaRepository
import com.example.caseritoapp.data.repository.ReservaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

data class HomeUiState(
    val categorias: List<CategoriaEntity> = emptyList(),
    val categoriaSeleccionada: String = "Todas",
    val ofertas: List<OfertaConDetalle> = emptyList(),
    val idsFavoritos: Set<Long> = emptySet(),
    val tiendas: List<TiendaEntity> = emptyList(),
    val isSyncing: Boolean = false,
    val mensajeError: String? = null
)

private data class SyncStatus(
    val isSyncing: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val ofertaRepository: OfertaRepository,
    private val tiendaRepository: TiendaRepository,
    private val favoritoRepository: FavoritoRepository,
    private val authRepository: AuthRepository,
    private val reservaRepository: ReservaRepository
) : ViewModel() {

    private val _categoriaSeleccionada = MutableStateFlow("Todas")
    private val _syncStatus = MutableStateFlow(SyncStatus(isSyncing = false))

    val uiState: StateFlow<HomeUiState> = combine(
        ofertaRepository.categorias,
        ofertaRepository.ofertasConDetalle,
        tiendaRepository.tiendasVerificadas,
        _categoriaSeleccionada,
        _syncStatus
    ) { categorias, ofertas, tiendas, categoriaSel, sync ->
        val ofertasFiltradas = if (categoriaSel == "Todas") {
            ofertas
        } else {
            ofertas.filter { it.nombreCategoria?.equals(categoriaSel, ignoreCase = true) == true }
        }

        HomeUiState(
            categorias = categorias,
            categoriaSeleccionada = categoriaSel,
            ofertas = ofertasFiltradas,
            tiendas = tiendas,
            isSyncing = sync.isSyncing,
            mensajeError = sync.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isSyncing = true)
    )

    private val _idsFavoritos = MutableStateFlow<Set<Long>>(emptySet())
    val idsFavoritos: StateFlow<Set<Long>> = _idsFavoritos.asStateFlow()
    private val _reservaEnCurso = MutableStateFlow<Long?>(null)
    val reservaEnCurso: StateFlow<Long?> = _reservaEnCurso.asStateFlow()
    private val _reservaFeedback = MutableStateFlow<String?>(null)
    val reservaFeedback: StateFlow<String?> = _reservaFeedback.asStateFlow()
    val isAuthenticated: StateFlow<Boolean> = authRepository.localUserId
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            authRepository.localUserId.flatMapLatest { userId ->
                userId?.let { favoritoRepository.getIdsFavoritos(it, "producto") }
                    ?: flowOf(emptyList())
            }.collect { ids ->
                _idsFavoritos.value = ids.toSet()
            }
        }
        sincronizarDatos()
    }

    fun seleccionarCategoria(categoria: String) {
        _categoriaSeleccionada.value = categoria
    }

    fun toggleFavorito(idOferta: Long) {
        viewModelScope.launch {
            val identity = authRepository.getOrSyncCurrentIdentity() ?: return@launch
            favoritoRepository.toggleFavorito(
                identity.localUserId,
                identity.authUserId,
                "producto",
                idOferta
            )
        }
    }

    fun reservarOferta(idProducto: Long, idUbicacion: Long) {
        if (_reservaEnCurso.value != null) return
        viewModelScope.launch {
            val identity = authRepository.getOrSyncCurrentIdentity()
            if (identity == null) {
                _reservaFeedback.value = "Inicia sesión para reservar esta oferta."
                return@launch
            }

            _reservaEnCurso.value = idProducto
            _reservaFeedback.value = null
            try {
                val reserva = reservaRepository.crearReserva(
                    localUserId = identity.localUserId,
                    idProducto = idProducto,
                    idUbicacion = idUbicacion,
                    cantidad = 1
                )
                _reservaFeedback.value =
                    "Reserva confirmada. Quedan ${reserva.stockRestante} unidades disponibles."
            } catch (error: Exception) {
                _reservaFeedback.value = error.message?.takeIf { it.isNotBlank() }
                    ?: "No se pudo completar la reserva. Intenta nuevamente."
            } finally {
                _reservaEnCurso.value = null
            }
        }
    }

    fun limpiarReservaFeedback() {
        _reservaFeedback.value = null
    }

    fun sincronizarDatos() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus(isSyncing = true, error = null)

            val resOfertas = ofertaRepository.syncOfertas()
            val resTiendas = tiendaRepository.syncTiendas()

            val errorMsg = if (resOfertas.isFailure || resTiendas.isFailure) {
                "Modo sin conexión: mostrando datos guardados"
            } else {
                null
            }
            _syncStatus.value = SyncStatus(isSyncing = false, error = errorMsg)
        }
    }

    fun limpiarError() {
        _syncStatus.value = _syncStatus.value.copy(error = null)
    }
}
