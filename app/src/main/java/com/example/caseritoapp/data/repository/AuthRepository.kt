package com.example.caseritoapp.data.repository

import com.example.caseritoapp.data.local.dao.UsuarioDao
import com.example.caseritoapp.data.local.entity.UsuarioEntity
import com.example.caseritoapp.data.remote.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
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

data class LocalAuthIdentity(
    val authUserId: String,
    val localUserId: Long
)

class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) {
    private val localUserSyncMutex = Mutex()
    val sessionStatus: Flow<SessionStatus> = supabase.auth.sessionStatus
    val localIdentity: Flow<LocalAuthIdentity?> = sessionStatus.map { getOrSyncCurrentIdentity() }
    val localUserId: Flow<Long?> = localIdentity.map { it?.localUserId }

    fun getCurrentUserInfo(): UserInfo? = supabase.auth.currentUserOrNull()

    suspend fun getOrSyncCurrentLocalUserId(): Long? = getOrSyncCurrentIdentity()?.localUserId

    suspend fun actualizarRolComercianteLocal() = withContext(Dispatchers.IO) {
        val identity = getOrSyncCurrentIdentity() ?: return@withContext
        val user = usuarioDao.getUsuarioPorAuthId(identity.authUserId) ?: return@withContext
        if (user.tipoUsuario != "staff_tienda") {
            usuarioDao.updateUsuario(user.copy(tipoUsuario = "staff_tienda"))
        }
    }

    suspend fun getOrSyncCurrentIdentity(): LocalAuthIdentity? = withContext(Dispatchers.IO) {
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
                ciudadPreferida = metadata?.get("ciudad_preferida")?.toString()?.replace("\"", "")
                    ?: existing?.ciudadPreferida ?: "Arequipa",
                estado = existing?.estado ?: "activo",
                fechaRegistro = userInfo.createdAt?.toString() ?: existing?.fechaRegistro.orEmpty(),
                ultimoLogin = now
            )

            val localUserId = if (existing == null) {
                usuarioDao.insertUsuario(usuario)
            } else {
                usuarioDao.updateUsuario(usuario)
                existing.id
            }
            LocalAuthIdentity(authUserId = authUserId, localUserId = localUserId)
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
        telefono: String,
        ciudad: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
                data = buildJsonObject {
                    put("nombre", nombre)
                    put("tipo_usuario", tipoUsuario)
                    put("telefono", telefono)
                    put("ciudad_preferida", ciudad)
                }
            }
            // Cuando Supabase tiene la confirmación de correo desactivada, signup crea una sesión.
            // Vincúlala inmediatamente con el perfil local; con confirmación activa no habrá sesión.
            if (supabase.auth.currentUserOrNull() != null) {
                getOrSyncCurrentLocalUserId()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifySignupEmailCode(email: String, token: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.verifyEmailOtp(
                type = OtpType.Email.EMAIL,
                email = email.trim(),
                token = token.trim()
            )
            getOrSyncCurrentLocalUserId()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendSignupEmailCode(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.resendEmail(OtpType.Email.SIGNUP, email.trim())
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
