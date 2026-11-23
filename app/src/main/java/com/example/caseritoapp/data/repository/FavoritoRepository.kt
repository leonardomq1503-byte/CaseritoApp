package com.example.caseritoapp.data.repository

import androidx.room.withTransaction
import com.example.caseritoapp.data.local.CaseritoDatabase
import com.example.caseritoapp.data.local.entity.FavoritoEntity
import com.example.caseritoapp.data.local.entity.SyncOperationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FavoritoRepository(
    private val database: CaseritoDatabase,
    private val scheduleSync: (String) -> Unit
) {
    private val favoritoDao get() = database.favoritoDao()

    fun getFavoritosPorUsuario(idUsuario: Long): Flow<List<FavoritoEntity>> =
        favoritoDao.getFavoritosPorUsuario(idUsuario)

    fun getIdsFavoritos(idUsuario: Long, tipo: String): Flow<List<Long>> =
        favoritoDao.getIdsFavoritos(idUsuario, normalizeType(tipo))

    fun contarFavoritos(idUsuario: Long): Flow<Int> = favoritoDao.contarFavoritos(idUsuario)

    suspend fun toggleFavorito(
        idUsuario: Long,
        authUserId: String,
        tipo: String,
        idObjeto: Long
    ): Boolean = withContext(Dispatchers.IO) {
        val tipoLocal = normalizeType(tipo)
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())

        val agregado = database.withTransaction {
            val existente = favoritoDao.getFavorito(idUsuario, tipoLocal, idObjeto)
            if (existente != null) {
                favoritoDao.deleteFavorito(idUsuario, tipoLocal, idObjeto)
                database.syncOperationDao().insert(
                    SyncOperationEntity(
                        authUserId = authUserId,
                        entity = "favorito",
                        action = "delete",
                        tipo = tipoLocal,
                        idObjeto = idObjeto,
                        fechaCreacion = now
                    )
                )
                false
            } else {
                favoritoDao.insertFavorito(
                    FavoritoEntity(
                        idUsuario = idUsuario,
                        tipo = tipoLocal,
                        idObjeto = idObjeto,
                        fechaCreacion = now
                    )
                )
                database.syncOperationDao().insert(
                    SyncOperationEntity(
                        authUserId = authUserId,
                        entity = "favorito",
                        action = "upsert",
                        tipo = tipoLocal,
                        idObjeto = idObjeto,
                        fechaCreacion = now
                    )
                )
                true
            }
        }
        scheduleSync(authUserId)
        agregado
    }

    private fun normalizeType(type: String): String = if (type == "oferta") "producto" else type
}
