package com.example.caseritoapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UbicacionDto(
    val id: Long,
    @SerialName("id_tienda")
    val idTienda: Long,
    @SerialName("nombre_sucursal")
    val nombreSucursal: String,
    val direccion: String,
    val ciudad: String = "Lima",
    val pais: String = "Perú",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val horarios: String? = null,
    val telefono: String? = null,
    val estado: String = "activa",
    @SerialName("fecha_creacion")
    val fechaCreacion: String? = null
)
