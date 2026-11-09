package com.example.caseritoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caseritoapp.data.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false,
    val registerSuccess: Boolean = false,
    val userEmail: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val sessionStatus: StateFlow<SessionStatus> = authRepository.sessionStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionStatus.Initializing
        )

    fun getCurrentUser(): UserInfo? = authRepository.getCurrentUserInfo()

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Por favor completa todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.signIn(email.trim(), pass)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginSuccess = true,
                    userEmail = email.trim()
                )
            } else {
                val error = result.exceptionOrNull()?.localizedMessage ?: "Error al iniciar sesión"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = if (error.contains("Invalid login", ignoreCase = true)) {
                        "Credenciales incorrectas. Verifica tu correo y contraseña."
                    } else {
                        error
                    }
                )
            }
        }
    }

    fun signUp(
        email: String,
        pass: String,
        nombre: String,
        tipoUsuario: String,
        telefono: String
    ) {
        if (email.isBlank() || pass.isBlank() || nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Por favor completa los campos requeridos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.signUp(
                email = email.trim(),
                pass = pass,
                nombre = nombre.trim(),
                tipoUsuario = tipoUsuario,
                telefono = telefono.trim()
            )
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    registerSuccess = true,
                    userEmail = email.trim()
                )
            } else {
                val error = result.exceptionOrNull()?.localizedMessage ?: "Error al registrar cuenta"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUiState()
        }
    }

    fun limpiarEstado() {
        _uiState.value = AuthUiState()
    }
}
