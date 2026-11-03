package com.example.caseritoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.caseritoapp.data.local.entity.TiendaEntity
import com.example.caseritoapp.data.local.entity.UbicacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TiendaDao {

    @Query("SELECT * FROM tiendas WHERE estado = 'verificada' ORDER BY nombre_comercial ASC")
    fun getTiendasVerificadas(): Flow<List<TiendaEntity>>

    @Query("SELECT * FROM tiendas WHERE id = :id LIMIT 1")
    suspend fun getTiendaPorId(id: Long): TiendaEntity?

    @Query("SELECT * FROM ubicaciones WHERE id_tienda = :idTienda AND estado = 'activa'")
    fun getUbicacionesPorTienda(idTienda: Long): Flow<List<UbicacionEntity>>

    @Query("SELECT * FROM ubicaciones WHERE estado = 'activa'")
    fun getTodasUbicacionesActivas(): Flow<List<UbicacionEntity>>

    @Query("SELECT * FROM ubicaciones WHERE id = :id LIMIT 1")
    suspend fun getUbicacionPorId(id: Long): UbicacionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTiendas(tiendas: List<TiendaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTienda(tienda: TiendaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUbicaciones(ubicaciones: List<UbicacionEntity>)

    @Update
    suspend fun updateTienda(tienda: TiendaEntity)
}
