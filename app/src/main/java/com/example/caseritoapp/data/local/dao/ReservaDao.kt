package com.example.caseritoapp.data.local.dao

import androidx.room.Dao
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.caseritoapp.data.local.entity.ReservaEntity
import kotlinx.coroutines.flow.Flow

data class ReservaConDetalle(
    @Embedded val reserva: ReservaEntity,
    @ColumnInfo(name = "nombre_producto") val nombreProducto: String,
    @ColumnInfo(name = "nombre_sucursal") val nombreSucursal: String
)

@Dao
interface ReservaDao {

    @Query("SELECT * FROM reservas WHERE id_usuario = :idUsuario ORDER BY fecha_reserva DESC")
    fun getReservasPorUsuario(idUsuario: Long): Flow<List<ReservaEntity>>

    @Query("""
        SELECT r.*, p.nombre AS nombre_producto, u.nombre_sucursal AS nombre_sucursal
        FROM reservas r
        INNER JOIN productos_oferta p ON p.id = r.id_producto
        INNER JOIN ubicaciones u ON u.id = r.id_ubicacion
        WHERE r.id_usuario = :idUsuario
        ORDER BY r.fecha_reserva DESC
    """)
    fun getReservasDetalladasPorUsuario(idUsuario: Long): Flow<List<ReservaConDetalle>>

    @Query("SELECT COUNT(*) FROM reservas WHERE id_usuario = :idUsuario AND estado = 'pendiente'")
    fun contarReservasActivas(idUsuario: Long): Flow<Int>

    @Query("SELECT * FROM reservas WHERE id = :id LIMIT 1")
    suspend fun getReservaPorId(id: Long): ReservaEntity?

    @Query("SELECT * FROM reservas WHERE id_remoto = :idRemoto LIMIT 1")
    suspend fun getReservaPorIdRemoto(idRemoto: Long): ReservaEntity?

    @Query("SELECT * FROM reservas WHERE id_ubicacion = :idUbicacion ORDER BY fecha_reserva DESC")
    fun getReservasPorSucursal(idUbicacion: Long): Flow<List<ReservaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReserva(reserva: ReservaEntity): Long

    @androidx.room.Update
    suspend fun updateReserva(reserva: ReservaEntity)

    @Query("UPDATE reservas SET estado = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, nuevoEstado: String)
}
