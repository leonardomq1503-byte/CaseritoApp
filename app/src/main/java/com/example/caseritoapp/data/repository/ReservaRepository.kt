package com.example.caseritoapp.data.repository

import androidx.room.withTransaction
import com.example.caseritoapp.data.local.CaseritoDatabase
import com.example.caseritoapp.data.local.entity.ReservaEntity
import com.example.caseritoapp.data.remote.SupabaseClientProvider
import com.example.caseritoapp.data.remote.dto.ReservaCloudDto
import com.example.caseritoapp.data.remote.dto.ReservaCanceladaRpcResult
import com.example.caseritoapp.data.remote.dto.ReservaRpcResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReservaCreada(
    val idRemoto: Long,
    val stockRestante: Int
)

class ReservaRepository(
    private val database: CaseritoDatabase,
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) {
    fun observarReservas(localUserId: Long): Flow<List<com.example.caseritoapp.data.local.dao.ReservaConDetalle>> =
        database.reservaDao().getReservasDetalladasPorUsuario(localUserId)

    suspend fun sincronizarReservas(authUserId: String, localUserId: Long) = withContext(Dispatchers.IO) {
        val remotas = supabase.from("reservas").select {
            filter { eq("id_usuario", authUserId) }
        }.decodeList<ReservaCloudDto>()

        val reservasDao = database.reservaDao()
        database.withTransaction {
            remotas.forEach { remoto ->
                // Room aplica claves foráneas; solo se importan reservas cuyos datos de catálogo están en caché.
                val oferta = database.ofertaDao().getOfertaPorId(remoto.idProducto) ?: return@forEach
                if (database.tiendaDao().getUbicacionPorId(remoto.idUbicacion) == null) return@forEach
                if (oferta.idUbicacion != remoto.idUbicacion) return@forEach

                val existente = reservasDao.getReservaPorIdRemoto(remoto.id)
                val reserva = ReservaEntity(
                    id = existente?.id ?: 0L,
                    idUsuario = localUserId,
                    idProducto = remoto.idProducto,
                    idUbicacion = remoto.idUbicacion,
                    cantidad = remoto.cantidad,
                    estado = remoto.estado,
                    fechaReserva = remoto.fechaReserva,
                    fechaExpiracion = remoto.fechaExpiracion,
                    idRemoto = remoto.id
                )
                if (existente == null) reservasDao.insertReserva(reserva)
                else reservasDao.updateReserva(reserva)
            }
        }
    }

    suspend fun crearReserva(
        localUserId: Long,
        idProducto: Long,
        idUbicacion: Long,
        cantidad: Int = 1
    ): ReservaCreada = withContext(Dispatchers.IO) {
        require(cantidad > 0) { "La cantidad debe ser mayor que cero" }

        val oferta = database.ofertaDao().getOfertaPorId(idProducto)
            ?: error("La oferta no está guardada en este dispositivo. Actualiza el catálogo e inténtalo nuevamente.")
        require(oferta.idUbicacion == idUbicacion) { "La oferta no pertenece a esta sucursal." }
        require(oferta.estado == "activo" && oferta.stockDisponible > 0) { "Esta oferta ya no tiene stock disponible." }

        val response = supabase.postgrest.rpc(
            function = "crear_reserva",
            parameters = buildJsonObject {
                put("p_id_producto", idProducto)
                put("p_id_ubicacion", idUbicacion)
                put("p_cantidad", cantidad)
            }
        )
        val creada = response.decodeList<ReservaRpcResult>().singleOrNull()
            ?: error("Supabase no devolvió la reserva creada")

        val fechaReserva = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())

        database.withTransaction {
            database.reservaDao().insertReserva(
                ReservaEntity(
                    idUsuario = localUserId,
                    idProducto = idProducto,
                    idUbicacion = idUbicacion,
                    cantidad = cantidad,
                    estado = "pendiente",
                    fechaReserva = fechaReserva,
                    idRemoto = creada.reservaId
                )
            )
            database.ofertaDao().actualizarStock(
                id = idProducto,
                stock = creada.stockRestante,
                estado = if (creada.stockRestante == 0) "agotado" else oferta.estado
            )
        }

        ReservaCreada(creada.reservaId, creada.stockRestante)
    }

    suspend fun cancelarReserva(idRemoto: Long): Int = withContext(Dispatchers.IO) {
        val local = database.reservaDao().getReservaPorIdRemoto(idRemoto)
            ?: error("No se encontró esta reserva en el dispositivo.")
        require(local.estado == "pendiente") { "Solo puedes cancelar reservas pendientes." }

        val response = supabase.postgrest.rpc(
            function = "cancelar_reserva",
            parameters = buildJsonObject { put("p_id_reserva", idRemoto) }
        )
        val cancelada = response.decodeList<ReservaCanceladaRpcResult>().singleOrNull()
            ?: error("Supabase no devolvió la reserva cancelada.")

        database.withTransaction {
            database.reservaDao().updateReserva(local.copy(estado = cancelada.reservaEstado))
            database.ofertaDao().actualizarStock(
                id = local.idProducto,
                stock = cancelada.stockRestante,
                estado = cancelada.ofertaEstado
            )
        }
        cancelada.stockRestante
    }
}
