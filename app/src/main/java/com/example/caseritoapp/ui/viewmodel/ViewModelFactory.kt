package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.caseritoapp.CaseritoApplication

class CaseritoViewModelFactory(
    private val application: CaseritoApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    ofertaRepository = application.ofertaRepository,
                    tiendaRepository = application.tiendaRepository,
                    favoritoRepository = application.favoritoRepository,
                    authRepository = application.authRepository,
                    reservaRepository = application.reservaRepository
                ) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(
                    ofertaRepository = application.ofertaRepository,
                    tiendaRepository = application.tiendaRepository,
                    favoritoRepository = application.favoritoRepository,
                    authRepository = application.authRepository,
                    reservaRepository = application.reservaRepository
                ) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(
                    authRepository = application.authRepository
                ) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(
                    authRepository = application.authRepository,
                    favoritoRepository = application.favoritoRepository,
                    reservaDao = application.database.reservaDao(),
                    usuarioDao = application.database.usuarioDao()
                ) as T
            }
            modelClass.isAssignableFrom(ReservasViewModel::class.java) -> {
                ReservasViewModel(
                    authRepository = application.authRepository,
                    reservaRepository = application.reservaRepository
                ) as T
            }
            modelClass.isAssignableFrom(AlertsViewModel::class.java) -> {
                AlertsViewModel(
                    notificacionRepository = application.notificacionRepository,
                    authRepository = application.authRepository,
                    personalDataSyncRepository = application.personalDataSyncRepository
                ) as T
            }
            modelClass.isAssignableFrom(MapViewModel::class.java) -> {
                MapViewModel(
                    tiendaRepository = application.tiendaRepository
                ) as T
            }
            modelClass.isAssignableFrom(StoreRegistrationViewModel::class.java) -> {
                StoreRegistrationViewModel(
                    tiendaRepository = application.tiendaRepository,
                    authRepository = application.authRepository
                ) as T
            }
            modelClass.isAssignableFrom(MerchantViewModel::class.java) -> {
                MerchantViewModel(
                    merchantRepository = application.merchantRepository,
                    authRepository = application.authRepository,
                    ofertaRepository = application.ofertaRepository,
                    tiendaRepository = application.tiendaRepository
                ) as T
            }
            modelClass.isAssignableFrom(ProductDetailViewModel::class.java) -> {
                ProductDetailViewModel(
                    ofertaRepository = application.ofertaRepository,
                    favoritoRepository = application.favoritoRepository,
                    authRepository = application.authRepository,
                    reservaRepository = application.reservaRepository
                ) as T
            }
            modelClass.isAssignableFrom(FavoritesViewModel::class.java) -> {
                FavoritesViewModel(
                    ofertaRepository = application.ofertaRepository,
                    tiendaRepository = application.tiendaRepository,
                    favoritoRepository = application.favoritoRepository,
                    authRepository = application.authRepository
                ) as T
            }
            modelClass.isAssignableFrom(StoreDetailViewModel::class.java) -> {
                StoreDetailViewModel(
                    tiendaRepository = application.tiendaRepository,
                    ofertaRepository = application.ofertaRepository
                ) as T
            }
            else -> throw IllegalArgumentException("ViewModel class desconocida: ${modelClass.name}")
        }
    }
}
