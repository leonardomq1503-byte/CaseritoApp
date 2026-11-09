package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.dao.OfertaConDetalle
import com.example.caseritoapp.data.local.entity.CategoriaEntity
import com.example.caseritoapp.data.local.entity.TiendaEntity
import com.example.caseritoapp.data.repository.OfertaRepository
import com.example.caseritoapp.data.repository.TiendaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class SearchUiState(
    val query: String = "",
    val categoriaSeleccionada: String = "Todas",
    val categorias: List<CategoriaEntity> = emptyList(),
    val ofertas: List<OfertaConDetalle> = emptyList(),
    val tiendas: List<TiendaEntity> = emptyList(),
    val totalResultados: Int = 0
)

class SearchViewModel(
    private val ofertaRepository: OfertaRepository,
    private val tiendaRepository: TiendaRepository
) : ViewModel() {

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
