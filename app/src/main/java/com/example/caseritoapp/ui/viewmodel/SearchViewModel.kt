package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.dao.OfertaConDetalle
import com.example.caseritoapp.data.local.entity.CategoriaEntity
import com.example.caseritoapp.data.local.entity.TiendaEntity
import com.example.caseritoapp.data.repository.OfertaRepository
import com.example.caseritoapp.data.repository.TiendaRepository
import com.example.caseritoapp.data.repository.FavoritoRepository
import com.example.caseritoapp.data.repository.AuthRepository
import com.example.caseritoapp.data.repository.ReservaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val categoriaSeleccionada: String = "Todas",
    val categorias: List<CategoriaEntity> = emptyList(),
    val ofertas: List<OfertaConDetalle> = emptyList(),
    val tiendas: List<TiendaEntity> = emptyList(),
    val totalResultados: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val ofertaRepository: OfertaRepository,
    private val tiendaRepository: TiendaRepository,
    private val favoritoRepository: FavoritoRepository,
    private val authRepository: AuthRepository,
    private val reservaRepository: ReservaRepository
) : ViewModel() {

    private val _favoritosProductos = MutableStateFlow<Set<Long>>(emptySet())
    val favoritosProductos: StateFlow<Set<Long>> = _favoritosProductos.asStateFlow()
    private val _favoritosTiendas = MutableStateFlow<Set<Long>>(emptySet())
    val favoritosTiendas: StateFlow<Set<Long>> = _favoritosTiendas.asStateFlow()
    val estaAutenticado: StateFlow<Boolean> = authRepository.localUserId.map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    private val _reservaEnCurso = MutableStateFlow<Long?>(null)
    val reservaEnCurso: StateFlow<Long?> = _reservaEnCurso.asStateFlow()
    private val _mensajeReserva = MutableStateFlow<String?>(null)
    val mensajeReserva: StateFlow<String?> = _mensajeReserva.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.localUserId.flatMapLatest { userId ->
                userId?.let { favoritoRepository.getIdsFavoritos(it, "producto") } ?: flowOf(emptyList())
            }.collect { _favoritosProductos.value = it.toSet() }
        }
        viewModelScope.launch {
            authRepository.localUserId.flatMapLatest { userId ->
                userId?.let { favoritoRepository.getIdsFavoritos(it, "tienda") } ?: flowOf(emptyList())
            }.collect { _favoritosTiendas.value = it.toSet() }
        }
    }

    fun alternarFavorito(tipo: String, id: Long) {
        viewModelScope.launch {
            val identidad = authRepository.getOrSyncCurrentIdentity() ?: return@launch
            favoritoRepository.toggleFavorito(identidad.localUserId, identidad.authUserId, tipo, id)
        }
    }

    fun reservar(idProducto: Long, idUbicacion: Long) {
        if (_reservaEnCurso.value != null) return
        viewModelScope.launch {
            val identidad = authRepository.getOrSyncCurrentIdentity()
            if (identidad == null) {
                _mensajeReserva.value = "Inicia sesión para reservar esta oferta."
                return@launch
            }
            _reservaEnCurso.value = idProducto
            _mensajeReserva.value = null
            runCatching { reservaRepository.crearReserva(identidad.localUserId, idProducto, idUbicacion) }
                .onSuccess { _mensajeReserva.value = "Reserva confirmada. Quedan ${it.stockRestante} unidades disponibles." }
                .onFailure { _mensajeReserva.value = it.message?.takeIf(String::isNotBlank) ?: "No se pudo reservar. Intenta nuevamente." }
            _reservaEnCurso.value = null
        }
    }

    fun limpiarMensajeReserva() { _mensajeReserva.value = null }

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow("Todas")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _tiendasResultados: Flow<List<TiendaEntity>> = _query.flatMapLatest { q ->
        if (q.isBlank()) {
            tiendaRepository.todasLasTiendas
        } else {
            tiendaRepository.buscarTiendas(q)
        }
    }

    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        _categoriaSeleccionada,
        ofertaRepository.categorias,
        ofertaRepository.ofertasConDetalle,
        _tiendasResultados
    ) { q, catSel, categorias, todasOfertas, tiendas ->
        val ofertasFiltradas = todasOfertas.filter { item ->
            val coincideTexto = q.isBlank() ||
                    item.oferta.nombre.contains(q, ignoreCase = true) ||
                    (item.oferta.descripcion?.contains(q, ignoreCase = true) == true) ||
                    (item.nombreTienda?.contains(q, ignoreCase = true) == true)

            val coincideCategoria = catSel == "Todas" ||
                    item.nombreCategoria?.equals(catSel, ignoreCase = true) == true

            coincideTexto && coincideCategoria
        }

        val tiendasFiltradas = tiendas.filter { tienda ->
            catSel == "Todas" || tienda.categoriaPrincipal?.equals(catSel, ignoreCase = true) == true
        }

        SearchUiState(
            query = q,
            categoriaSeleccionada = catSel,
            categorias = categorias,
            ofertas = ofertasFiltradas,
            tiendas = tiendasFiltradas,
            totalResultados = ofertasFiltradas.size + tiendasFiltradas.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onCategoriaChange(categoria: String) {
        _categoriaSeleccionada.value = categoria
    }
}
