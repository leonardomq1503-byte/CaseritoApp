package com.example.caseritoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.caseritoapp.data.local.entity.ReservaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {

    @Query("SELECT * FROM reservas WHERE id_usuario = :idUsuario ORDER BY fecha_reserva DESC")
    fun getReservasPorUsuario(idUsuario: Long): Flow<List<ReservaEntity>>

    @Query("SELECT COUNT(*) FROM reservas WHERE id_usuario = :idUsuario AND estado = 'pendiente'")
    fun contarReservasActivas(idUsuario: Long): Flow<Int>

    @Query("SELECT * FROM reservas WHERE id = :id LIMIT 1")
    suspend fun getReservaPorId(id: Long): ReservaEntity?

    @Query("SELECT * FROM reservas WHERE id_ubicacion = :idUbicacion ORDER BY fecha_reserva DESC")
    fun getReservasPorSucursal(idUbicacion: Long): Flow<List<ReservaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReserva(reserva: ReservaEntity): Long

    @Query("UPDATE reservas SET estado = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, nuevoEstado: String)
}
