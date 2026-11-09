package com.example.caseritoapp.data.repository

import com.example.caseritoapp.data.local.dao.TiendaDao
import com.example.caseritoapp.data.local.entity.TiendaEntity
import com.example.caseritoapp.data.local.entity.UbicacionEntity
import com.example.caseritoapp.data.mapper.toEntity
import com.example.caseritoapp.data.remote.SupabaseClientProvider
import com.example.caseritoapp.data.remote.dto.TiendaDto
import com.example.caseritoapp.data.remote.dto.UbicacionDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TiendaRepository(
    private val tiendaDao: TiendaDao,
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) {
    // Single Source of Truth
    val tiendasVerificadas: Flow<List<TiendaEntity>> = tiendaDao.getTiendasVerificadas()
    val todasLasTiendas: Flow<List<TiendaEntity>> = tiendaDao.getTodasLasTiendas()
    val ubicacionesActivas: Flow<List<UbicacionEntity>> = tiendaDao.getTodasUbicacionesActivas()

    fun buscarTiendas(query: String): Flow<List<TiendaEntity>> {
        return tiendaDao.buscarTiendas(query)
    }

    fun getUbicacionesPorTienda(idTienda: Long): Flow<List<UbicacionEntity>> {
        return tiendaDao.getUbicacionesPorTienda(idTienda)
    }

    suspend fun getTiendaPorId(id: Long): TiendaEntity? {
        return tiendaDao.getTiendaPorId(id)
    }

    // Sincronización en segundo plano con Supabase
    suspend fun syncTiendas(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Descargar tiendas
            val remoteTiendas = supabase.from("tiendas")
                .select()
                .decodeList<TiendaDto>()
            if (remoteTiendas.isNotEmpty()) {
                tiendaDao.insertTiendas(remoteTiendas.map { it.toEntity() })
            }

            // 2. Descargar sucursales / ubicaciones
            val remoteUbicaciones = supabase.from("ubicaciones")
                .select()
                .decodeList<UbicacionDto>()
            if (remoteUbicaciones.isNotEmpty()) {
                tiendaDao.insertUbicaciones(remoteUbicaciones.map { it.toEntity() })
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
