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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val nombre: String = "Caserito Invitado",
    val email: String = "invitado@caserito.pe",
    val tipoUsuario: String = "cliente",
    val rolBadge: String = "Cliente Caserito",
    val totalFavoritos: Int = 0,
    val totalReservasActivas: Int = 0,
    val estaAutenticado: Boolean = false
)

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
            val userInfo = authRepository.getCurrentUserInfo()
            val userId = favoritoRepository.getOrCreateDefaultUserId()

            combine(
                favoritoRepository.contarFavoritos(userId),
                reservaDao.contarReservasActivas(userId),
                usuarioDao.getUsuarioPorId(userId)
            ) { favoritosCount, reservasCount, usuarioLocal ->
                val nombre = userInfo?.userMetadata?.get("nombre")?.toString()?.replace("\"", "")
                    ?: usuarioLocal?.nombre
                    ?: "Usuario Caserito"

                val email = userInfo?.email ?: usuarioLocal?.email ?: "usuario@caserito.pe"
                val tipo = userInfo?.userMetadata?.get("tipo_usuario")?.toString()?.replace("\"", "")
                    ?: usuarioLocal?.tipoUsuario
                    ?: "cliente"

                val badge = if (tipo == "staff_tienda") "Comercio Caserito" else "Cliente Caserito"

                ProfileUiState(
                    nombre = nombre,
                    email = email,
                    tipoUsuario = tipo,
                    rolBadge = badge,
                    totalFavoritos = favoritosCount,
                    totalReservasActivas = reservasCount,
                    estaAutenticado = userInfo != null
                )
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
