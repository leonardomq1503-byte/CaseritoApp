package com.example.caseritoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.caseritoapp.data.local.entity.FavoritoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritoDao {

    @Query("SELECT * FROM favoritos WHERE id_usuario = :idUsuario ORDER BY fecha_creacion DESC")
    fun getFavoritosPorUsuario(idUsuario: Long): Flow<List<FavoritoEntity>>

    @Query("SELECT * FROM favoritos WHERE id_usuario = :idUsuario AND tipo = :tipo ORDER BY fecha_creacion DESC")
    fun getFavoritosPorTipo(idUsuario: Long, tipo: String): Flow<List<FavoritoEntity>>

    @Query("SELECT * FROM favoritos WHERE id_usuario = :idUsuario AND tipo = :tipo AND id_objeto = :idObjeto LIMIT 1")
    suspend fun getFavorito(idUsuario: Long, tipo: String, idObjeto: Long): FavoritoEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favoritos WHERE id_usuario = :idUsuario AND tipo = :tipo AND id_objeto = :idObjeto)")
    fun esFavorito(idUsuario: Long, tipo: String, idObjeto: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorito(favorito: FavoritoEntity): Long

    @Query("DELETE FROM favoritos WHERE id_usuario = :idUsuario AND tipo = :tipo AND id_objeto = :idObjeto")
    suspend fun deleteFavorito(idUsuario: Long, tipo: String, idObjeto: Long)

    @Query("DELETE FROM favoritos WHERE id = :id")
    suspend fun deleteFavoritoPorId(id: Long)
}
