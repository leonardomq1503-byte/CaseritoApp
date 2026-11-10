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
                    favoritoRepository = application.favoritoRepository
                ) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(
                    ofertaRepository = application.ofertaRepository,
                    tiendaRepository = application.tiendaRepository
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
            modelClass.isAssignableFrom(AlertsViewModel::class.java) -> {
                AlertsViewModel(
                    notificacionRepository = application.notificacionRepository,
                    favoritoRepository = application.favoritoRepository
                ) as T
            }
            modelClass.isAssignableFrom(MapViewModel::class.java) -> {
                MapViewModel(
                    tiendaRepository = application.tiendaRepository
                ) as T
            }
            else -> throw IllegalArgumentException("ViewModel class desconocida: ${modelClass.name}")
        }
    }
}
