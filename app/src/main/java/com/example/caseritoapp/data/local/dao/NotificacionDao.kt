package com.example.caseritoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.caseritoapp.data.local.entity.NotificacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {

    @Query("SELECT * FROM notificaciones WHERE id_usuario = :idUsuario ORDER BY fecha_creacion DESC")
    fun getNotificacionesPorUsuario(idUsuario: Long): Flow<List<NotificacionEntity>>

    @Query("SELECT COUNT(*) FROM notificaciones WHERE id_usuario = :idUsuario AND leida = 0")
    fun getCantidadNoLeidas(idUsuario: Long): Flow<Int>

    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id AND id_usuario = :idUsuario")
    suspend fun marcarComoLeida(id: Long, idUsuario: Long)

    @Query("UPDATE notificaciones SET leida = 1 WHERE id_usuario = :idUsuario")
    suspend fun marcarTodasComoLeidas(idUsuario: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificaciones(notificaciones: List<NotificacionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificacion(notificacion: NotificacionEntity): Long
}
