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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) {
    private val localUserSyncMutex = Mutex()
    val sessionStatus: Flow<SessionStatus> = supabase.auth.sessionStatus
    val localUserId: Flow<Long?> = sessionStatus.map { getOrSyncCurrentLocalUserId() }

    fun getCurrentUserInfo(): UserInfo? = supabase.auth.currentUserOrNull()

    suspend fun getOrSyncCurrentLocalUserId(): Long? = withContext(Dispatchers.IO) {
        localUserSyncMutex.withLock {
            val userInfo = supabase.auth.currentUserOrNull() ?: return@withLock null
            val authUserId = userInfo.id
            val existing = usuarioDao.getUsuarioPorAuthId(authUserId)
                ?: userInfo.email?.let { usuarioDao.getUsuarioPorEmail(it) }
            val metadata = userInfo.userMetadata
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
            val usuario = UsuarioEntity(
                id = existing?.id ?: 0L,
                authUserId = authUserId,
                tipoUsuario = metadata?.get("tipo_usuario")?.toString()?.replace("\"", "")
                    ?: existing?.tipoUsuario ?: "cliente",
                nombre = metadata?.get("nombre")?.toString()?.replace("\"", "")
                    ?: existing?.nombre ?: "Usuario",
                email = userInfo.email ?: existing?.email,
                telefono = userInfo.phone ?: existing?.telefono,
                passwordHash = "",
                ciudadPreferida = existing?.ciudadPreferida,
                estado = existing?.estado ?: "activo",
                fechaRegistro = userInfo.createdAt?.toString() ?: existing?.fechaRegistro.orEmpty(),
                ultimoLogin = now
            )

            if (existing == null) {
                usuarioDao.insertUsuario(usuario)
            } else {
                usuarioDao.updateUsuario(usuario)
                existing.id
            }
        }
    }

    suspend fun signIn(email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
            getOrSyncCurrentLocalUserId()
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
