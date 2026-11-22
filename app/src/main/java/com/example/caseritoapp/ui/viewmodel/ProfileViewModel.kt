package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.local.dao.ReservaDao
import com.example.caseritoapp.data.local.dao.UsuarioDao
import com.example.caseritoapp.data.repository.AuthRepository
import com.example.caseritoapp.data.repository.FavoritoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class ProfileUiState(
    val nombre: String = "Inicia sesión",
    val email: String = "",
    val tipoUsuario: String = "cliente",
    val rolBadge: String = "Cliente Caserito",
    val totalFavoritos: Int = 0,
    val totalReservasActivas: Int = 0,
    val estaAutenticado: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val favoritoRepository: FavoritoRepository,
    private val reservaDao: ReservaDao,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            authRepository.localUserId.flatMapLatest { userId ->
                if (userId == null) {
                    flowOf(ProfileUiState())
                } else {
                    combine(
                        favoritoRepository.contarFavoritos(userId),
                        reservaDao.contarReservasActivas(userId),
                        usuarioDao.getUsuarioPorId(userId)
                    ) { favoritosCount, reservasCount, usuarioLocal ->
                        val userInfo = authRepository.getCurrentUserInfo()
                        val tipo = usuarioLocal?.tipoUsuario ?: "cliente"
                        val badge = if (tipo == "staff_tienda") "Comercio Caserito" else "Cliente Caserito"

                        ProfileUiState(
                            nombre = usuarioLocal?.nombre ?: "Usuario Caserito",
                            email = usuarioLocal?.email.orEmpty(),
                            tipoUsuario = tipo,
                            rolBadge = badge,
                            totalFavoritos = favoritosCount,
                            totalReservasActivas = reservasCount,
                            estaAutenticado = userInfo != null
                        )
                    }
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun cerrarSesion(onCompletado: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = ProfileUiState()
            onCompletado()
        }
    }
}
