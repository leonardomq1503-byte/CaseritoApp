package com.example.caseritoapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReservaRpcResult(
    @SerialName("reserva_id") val reservaId: Long,
    @SerialName("stock_restante") val stockRestante: Int
)

@Serializable
data class ReservaCloudDto(
    val id: Long,
    @SerialName("id_usuario") val idUsuario: String,
    @SerialName("id_producto") val idProducto: Long,
    @SerialName("id_ubicacion") val idUbicacion: Long,
    val cantidad: Int,
    val estado: String,
    @SerialName("fecha_reserva") val fechaReserva: String,
    @SerialName("fecha_expiracion") val fechaExpiracion: String? = null
)
