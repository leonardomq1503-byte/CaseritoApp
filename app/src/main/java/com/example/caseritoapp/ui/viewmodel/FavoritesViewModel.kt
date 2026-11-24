package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.dao.OfertaConDetalle
import com.example.caseritoapp.data.local.entity.TiendaEntity
import com.example.caseritoapp.data.repository.AuthRepository
import com.example.caseritoapp.data.repository.FavoritoRepository
import com.example.caseritoapp.data.repository.OfertaRepository
import com.example.caseritoapp.data.repository.TiendaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val authenticated: Boolean = false,
    val products: List<OfertaConDetalle> = emptyList(),
    val stores: List<TiendaEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModel(
    private val ofertaRepository: OfertaRepository,
    private val tiendaRepository: TiendaRepository,
    private val favoritoRepository: FavoritoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val productIds = authRepository.localUserId.flatMapLatest { id ->
        id?.let { favoritoRepository.getIdsFavoritos(it, "producto") } ?: flowOf(emptyList())
    }.map { it.toSet() }
    private val storeIds = authRepository.localUserId.flatMapLatest { id ->
        id?.let { favoritoRepository.getIdsFavoritos(it, "tienda") } ?: flowOf(emptyList())
    }.map { it.toSet() }

    val uiState: StateFlow<FavoritesUiState> = combine(
        authRepository.localUserId,
        ofertaRepository.ofertasConDetalle,
        tiendaRepository.todasLasTiendas,
        productIds,
        storeIds
    ) { userId, offers, stores, savedProductIds, savedStoreIds ->
        FavoritesUiState(
            authenticated = userId != null,
            products = offers.filter { it.oferta.id in savedProductIds },
            stores = stores.filter { it.id in savedStoreIds }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritesUiState())

    fun quitarProducto(id: Long) = quitar("producto", id)
    fun quitarTienda(id: Long) = quitar("tienda", id)

    private fun quitar(tipo: String, id: Long) {
        viewModelScope.launch {
            val identity = authRepository.getOrSyncCurrentIdentity() ?: return@launch
            favoritoRepository.toggleFavorito(identity.localUserId, identity.authUserId, tipo, id)
        }
    }
}
