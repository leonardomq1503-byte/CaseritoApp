package com.example.caseritoapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistrarTiendaRpcResult(
    @SerialName("id_tienda") val idTienda: Long,
    @SerialName("id_ubicacion") val idUbicacion: Long
)
