package com.example.caseritoapp.data.repository

import com.example.caseritoapp.data.local.dao.TiendaDao
import com.example.caseritoapp.data.local.entity.TiendaEntity
import com.example.caseritoapp.data.local.entity.UbicacionEntity
import com.example.caseritoapp.data.mapper.toEntity
import com.example.caseritoapp.data.remote.SupabaseClientProvider
import com.example.caseritoapp.data.remote.dto.TiendaDto
import com.example.caseritoapp.data.remote.dto.UbicacionDto
import com.example.caseritoapp.data.remote.dto.RegistrarTiendaRpcResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class RegistroTienda(
    val nombreComercial: String,
    val nombreSucursal: String,
    val direccion: String,
    val ciudad: String,
    val razonSocial: String? = null,
    val tipoNegocio: String? = null,
    val categoriaPrincipal: String? = null,
    val descripcion: String? = null,
    val web: String? = null,
    val telefono: String? = null,
    val horarios: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

data class TiendaRegistrada(val idTienda: Long, val idUbicacion: Long)

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

    fun observarTiendaPorId(id: Long): Flow<TiendaEntity?> = tiendaDao.observarTiendaPorId(id)

    suspend fun registrarTienda(datos: RegistroTienda): TiendaRegistrada = withContext(Dispatchers.IO) {
        val resultado = supabase.postgrest.rpc(
            function = "registrar_tienda",
            parameters = buildJsonObject {
                put("p_nombre_comercial", datos.nombreComercial)
                put("p_nombre_sucursal", datos.nombreSucursal)
                put("p_direccion", datos.direccion)
                put("p_ciudad", datos.ciudad)
                put("p_razon_social", datos.razonSocial)
                put("p_tipo_negocio", datos.tipoNegocio)
                put("p_categoria_principal", datos.categoriaPrincipal)
                put("p_descripcion", datos.descripcion)
                put("p_web", datos.web)
                put("p_telefono", datos.telefono)
                put("p_horarios", datos.horarios)
                put("p_latitud", datos.latitud)
                put("p_longitud", datos.longitud)
            }
        ).decodeList<RegistrarTiendaRpcResult>().singleOrNull()
            ?: error("Supabase no devolvió la tienda registrada")

        TiendaRegistrada(resultado.idTienda, resultado.idUbicacion)
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
