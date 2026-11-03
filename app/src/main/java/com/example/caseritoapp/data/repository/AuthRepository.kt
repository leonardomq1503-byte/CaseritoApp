package com.example.caseritoapp.data.repository

import com.example.caseritoapp.data.local.dao.UsuarioDao
import com.example.caseritoapp.data.local.entity.UsuarioEntity
import com.example.caseritoapp.data.remote.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) {
    val sessionStatus: Flow<SessionStatus> = supabase.auth.sessionStatus

    fun getCurrentUserInfo(): UserInfo? = supabase.auth.currentUserOrNull()

    suspend fun signIn(email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
            // Guardar usuario en Room para acceso offline
            val userInfo = supabase.auth.currentUserOrNull()
            if (userInfo != null) {
                usuarioDao.insertUsuario(
                    UsuarioEntity(
                        tipoUsuario = userInfo.userMetadata?.get("tipo_usuario")?.toString()?.replace("\"", "") ?: "cliente",
                        nombre = userInfo.userMetadata?.get("nombre")?.toString()?.replace("\"", "") ?: "Usuario",
                        email = userInfo.email,
                        telefono = userInfo.phone,
                        passwordHash = "",
                        fechaRegistro = userInfo.createdAt?.toString() ?: ""
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(
        email: String,
        pass: String,
        nombre: String,
        tipoUsuario: String,
        telefono: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
                data = buildJsonObject {
                    put("nombre", nombre)
                    put("tipo_usuario", tipoUsuario)
                    put("telefono", telefono)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
