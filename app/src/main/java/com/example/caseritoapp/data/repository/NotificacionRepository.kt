package com.example.caseritoapp.data.repository

import androidx.room.withTransaction
import com.example.caseritoapp.data.local.CaseritoDatabase
import com.example.caseritoapp.data.local.entity.NotificacionEntity
import com.example.caseritoapp.data.local.entity.SyncOperationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificacionRepository(
    private val database: CaseritoDatabase,
    private val scheduleSync: (String) -> Unit
) {
    private val notificacionDao get() = database.notificacionDao()

    fun getNotificacionesPorUsuario(idUsuario: Long): Flow<List<NotificacionEntity>> =
        notificacionDao.getNotificacionesPorUsuario(idUsuario)

    fun getCantidadNoLeidas(idUsuario: Long): Flow<Int> =
        notificacionDao.getCantidadNoLeidas(idUsuario)

    suspend fun marcarComoLeida(id: Long, idUsuario: Long, authUserId: String) =
        withContext(Dispatchers.IO) {
            val remoteId = database.withTransaction {
                val local = notificacionDao.getNotificacion(id, idUsuario) ?: return@withTransaction null
                if (local.leida) return@withTransaction null

                notificacionDao.marcarComoLeida(id, idUsuario)
                local.idRemoto?.let {
                    database.syncOperationDao().insert(
                        SyncOperationEntity(
                            authUserId = authUserId,
                            entity = "notificacion",
                            action = "read",
                            idRemoto = it,
                            fechaCreacion = ahora()
                        )
                    )
                }
                local.idRemoto
            }
            if (remoteId != null) scheduleSync(authUserId)
        }

    suspend fun marcarTodasComoLeidas(idUsuario: Long, authUserId: String) =
        withContext(Dispatchers.IO) {
            val changed = database.withTransaction {
                val noLeidas = notificacionDao.getNotificacionesPorUsuarioUnaVez(idUsuario)
                    .filter { !it.leida }
                notificacionDao.marcarTodasComoLeidas(idUsuario)
                noLeidas.mapNotNull { notificacion ->
                    notificacion.idRemoto?.also { remoteId ->
                        database.syncOperationDao().insert(
                            SyncOperationEntity(
                                authUserId = authUserId,
                                entity = "notificacion",
                                action = "read",
                                idRemoto = remoteId,
                                fechaCreacion = ahora()
                            )
                        )
                    }
                }
            }
            if (changed.isNotEmpty()) scheduleSync(authUserId)
        }

    /** Inserta una alerta local de demostración; las alertas remotas las genera el backend. */
    suspend fun agregarNotificacion(
        idUsuario: Long,
        titulo: String,
        mensaje: String,
        tipo: String = "nueva_oferta_tienda",
        idObjeto: Long? = null,
        tipoObjeto: String? = null
    ): Long = withContext(Dispatchers.IO) {
        notificacionDao.insertNotificacion(
            NotificacionEntity(
                idUsuario = idUsuario,
                tipo = tipo,
                titulo = titulo,
                mensaje = mensaje,
                idObjeto = idObjeto,
                tipoObjeto = tipoObjeto,
                leida = false,
                fechaCreacion = ahora()
            )
        )
    }

    private fun ahora(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
}
