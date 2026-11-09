package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.dao.OfertaConDetalle
import com.example.caseritoapp.data.local.entity.CategoriaEntity
import com.example.caseritoapp.data.local.entity.TiendaEntity
import com.example.caseritoapp.data.repository.FavoritoRepository
import com.example.caseritoapp.data.repository.OfertaRepository
import com.example.caseritoapp.data.repository.TiendaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

class HomeViewModel(
    private val ofertaRepository: OfertaRepository,
    private val tiendaRepository: TiendaRepository,
    private val favoritoRepository: FavoritoRepository
) : ViewModel() {

    private val _categoriaSeleccionada = MutableStateFlow("Todas")
    private val _syncStatus = MutableStateFlow(SyncStatus(isSyncing = false))
    private var userId: Long = 1L

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

    init {
        viewModelScope.launch {
            userId = favoritoRepository.getOrCreateDefaultUserId()
            favoritoRepository.getIdsFavoritos(userId, "oferta").collect { ids ->
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
            favoritoRepository.toggleFavorito(userId, "oferta", idOferta)
        }
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
