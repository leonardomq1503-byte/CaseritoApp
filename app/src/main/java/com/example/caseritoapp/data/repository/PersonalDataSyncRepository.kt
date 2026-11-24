package com.example.caseritoapp.data.repository

import androidx.room.withTransaction
import com.example.caseritoapp.data.local.CaseritoDatabase
import com.example.caseritoapp.data.local.entity.FavoritoEntity
import com.example.caseritoapp.data.remote.SupabaseClientProvider
import com.example.caseritoapp.data.remote.dto.FavoritoCloudDto
import com.example.caseritoapp.data.remote.dto.NotificacionCloudDto
import com.example.caseritoapp.data.remote.dto.NotificacionLeidaUpdate
import com.example.caseritoapp.data.local.entity.NotificacionEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersonalDataSyncRepository(
    private val database: CaseritoDatabase,
    private val supabase: SupabaseClient = SupabaseClientProvider.client,
    private val onNewNotification: (NotificacionEntity) -> Unit = {}
) {
    suspend fun sincronizar(authUserId: String, localUserId: Long) = withContext(Dispatchers.IO) {
        val operationDao = database.syncOperationDao()

        for (operation in operationDao.pendientes(authUserId)) {
            when (operation.entity to operation.action) {
                "favorito" to "upsert" -> {
                    val tipoRemoto = operation.tipo.toRemoteFavoriteType()
                    supabase.from("favoritos").upsert(
                        FavoritoCloudDto(
                            idUsuario = authUserId,
                            tipo = tipoRemoto,
                            idObjeto = requireNotNull(operation.idObjeto),
                            fechaCreacion = operation.fechaCreacion
                        )
                    ) {
                        onConflict = "id_usuario,tipo,id_objeto"
                    }
                }
                "favorito" to "delete" -> {
                    supabase.from("favoritos").delete {
                        filter {
                            eq("id_usuario", authUserId)
                            eq("tipo", operation.tipo.toRemoteFavoriteType())
                            eq("id_objeto", requireNotNull(operation.idObjeto))
                        }
                    }
                }
                "notificacion" to "read" -> {
                    supabase.from("notificaciones").update(NotificacionLeidaUpdate(leida = true)) {
                        filter {
                            eq("id_usuario", authUserId)
                            eq("id", requireNotNull(operation.idRemoto))
                        }
                    }
                }
                else -> error("Operación de sincronización desconocida: ${operation.entity}/${operation.action}")
            }
            operationDao.eliminar(operation.id)
        }

        sincronizarFavoritos(authUserId, localUserId)
        sincronizarNotificaciones(authUserId, localUserId)
    }

    private suspend fun sincronizarFavoritos(authUserId: String, localUserId: Long) {
        val remotos = supabase.from("favoritos").select {
            filter { eq("id_usuario", authUserId) }
        }.decodeList<FavoritoCloudDto>()
            .filter { it.tipo == "producto" || it.tipo == "tienda" }

        val dao = database.favoritoDao()
        val remotosPorClave = remotos.associateBy { it.tipo to it.idObjeto }
        database.withTransaction {
            remotos.forEach { remoto ->
                dao.insertFavoritoSiNoExiste(
                    FavoritoEntity(
                        idUsuario = localUserId,
                        tipo = remoto.tipo,
                        idObjeto = remoto.idObjeto,
                        fechaCreacion = remoto.fechaCreacion
                    )
                )
            }

            dao.getFavoritosSincronizables(localUserId)
                .filter { (it.tipo to it.idObjeto) !in remotosPorClave }
                .forEach { dao.deleteFavorito(localUserId, it.tipo, it.idObjeto) }
        }
    }

    private suspend fun sincronizarNotificaciones(authUserId: String, localUserId: Long) {
        val remotas = supabase.from("notificaciones").select {
            filter { eq("id_usuario", authUserId) }
        }.decodeList<NotificacionCloudDto>()

        val dao = database.notificacionDao()
        val nuevas = mutableListOf<NotificacionEntity>()
        database.withTransaction {
            remotas.forEach { remoto ->
                val actual = dao.getNotificacionPorIdRemoto(remoto.id)
                val notificacion = NotificacionEntity(
                    id = actual?.id ?: 0L,
                    idUsuario = localUserId,
                    tipo = remoto.tipo,
                    titulo = remoto.titulo,
                    mensaje = remoto.mensaje,
                    idObjeto = remoto.idObjeto,
                    tipoObjeto = remoto.tipoObjeto,
                    leida = remoto.leida,
                    fechaCreacion = remoto.fechaCreacion,
                    idRemoto = remoto.id
                )
                if (actual == null) {
                    dao.insertNotificacion(notificacion)
                    if (!remoto.leida) nuevas += notificacion
                }
                else dao.updateNotificacion(notificacion)
            }
        }
        nuevas.forEach(onNewNotification)
    }

}

private fun String?.toRemoteFavoriteType(): String = when (this) {
    "oferta", "producto" -> "producto"
    "tienda" -> "tienda"
    else -> error("Tipo de favorito no sincronizable: $this")
}
