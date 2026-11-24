package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.entity.TiendaEntity
import com.example.caseritoapp.data.local.entity.UbicacionEntity
import com.example.caseritoapp.data.local.dao.OfertaConDetalle
import com.example.caseritoapp.data.repository.OfertaRepository
import com.example.caseritoapp.data.repository.TiendaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class StoreDetailUiState(
    val tienda: TiendaEntity? = null,
    val ubicaciones: List<UbicacionEntity> = emptyList(),
    val ofertas: List<OfertaConDetalle> = emptyList(),
    val cargando: Boolean = true
)

class StoreDetailViewModel(
    private val tiendaRepository: TiendaRepository,
    private val ofertaRepository: OfertaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoreDetailUiState())
    val uiState = _uiState.asStateFlow()
    private var observeJob: Job? = null

    fun cargar(idTienda: Long) {
        observeJob?.cancel()
        _uiState.value = StoreDetailUiState()
        observeJob = viewModelScope.launch {
            combine(
                tiendaRepository.observarTiendaPorId(idTienda),
                tiendaRepository.getUbicacionesPorTienda(idTienda),
                ofertaRepository.observarOfertasActivasPorTienda(idTienda)
            ) { tienda, ubicaciones, ofertas ->
                StoreDetailUiState(tienda, ubicaciones, ofertas, cargando = false)
            }.collect { _uiState.value = it }
        }
    }
}
