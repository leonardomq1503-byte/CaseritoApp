package com.example.caseritoapp.data.repository

import com.example.caseritoapp.data.remote.SupabaseClientProvider
import com.example.caseritoapp.data.remote.dto.MerchantLocationDto
import com.example.caseritoapp.data.remote.dto.MerchantOfferCreatedDto
import com.example.caseritoapp.data.remote.dto.MerchantOfferDto
import com.example.caseritoapp.data.remote.dto.MerchantStoreDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class NuevaOferta(
    val idUbicacion: Long,
    val nombre: String,
    val descripcion: String?,
    val precioOriginal: Double,
    val precioOferta: Double,
    val stock: Int,
    val fechaFin: String
)

class MerchantRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun misTiendas(): List<MerchantStoreDto> = withContext(Dispatchers.IO) {
        supabase.postgrest.rpc("listar_mis_tiendas").decodeList<MerchantStoreDto>()
    }

    suspend fun misUbicaciones(): List<MerchantLocationDto> = withContext(Dispatchers.IO) {
        supabase.postgrest.rpc("listar_mis_ubicaciones").decodeList<MerchantLocationDto>()
    }

    suspend fun misOfertas(): List<MerchantOfferDto> = withContext(Dispatchers.IO) {
        supabase.postgrest.rpc("listar_mis_ofertas").decodeList<MerchantOfferDto>()
    }

    suspend fun crearOferta(datos: NuevaOferta): Long = withContext(Dispatchers.IO) {
        require(datos.nombre.isNotBlank()) { "Escribe el nombre del producto." }
        require(datos.precioOriginal > 0 && datos.precioOferta > 0) { "Los precios deben ser mayores que cero." }
        require(datos.precioOferta < datos.precioOriginal) { "El precio de oferta debe ser menor que el precio regular." }
        require(datos.stock >= 0) { "El stock no puede ser negativo." }
        supabase.postgrest.rpc(
            function = "crear_oferta_tienda",
            parameters = buildJsonObject {
                put("p_id_ubicacion", datos.idUbicacion)
                put("p_nombre", datos.nombre.trim())
                put("p_descripcion", datos.descripcion)
                put("p_precio_original", datos.precioOriginal)
                put("p_precio_oferta", datos.precioOferta)
                put("p_stock_disponible", datos.stock)
                put("p_fecha_fin", datos.fechaFin)
            }
        ).decodeList<MerchantOfferCreatedDto>().single().idProducto
    }

    suspend fun cambiarEstadoOferta(idProducto: Long, nuevoEstado: String) = withContext(Dispatchers.IO) {
        require(nuevoEstado == "activo" || nuevoEstado == "pausado")
        supabase.postgrest.rpc(
            function = "cambiar_estado_oferta_tienda",
            parameters = buildJsonObject {
                put("p_id_producto", idProducto)
                put("p_nuevo_estado", nuevoEstado)
            }
        )
    }
}
