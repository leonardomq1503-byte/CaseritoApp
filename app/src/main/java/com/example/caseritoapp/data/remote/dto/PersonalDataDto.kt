package com.example.caseritoapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoritoCloudDto(
    @SerialName("id_usuario") val idUsuario: String,
    val tipo: String,
    @SerialName("id_objeto") val idObjeto: Long,
    @SerialName("fecha_creacion") val fechaCreacion: String
)

@Serializable
data class NotificacionCloudDto(
    val id: Long,
    @SerialName("id_usuario") val idUsuario: String,
    val tipo: String,
    val titulo: String,
    val mensaje: String? = null,
    @SerialName("id_objeto") val idObjeto: Long? = null,
    @SerialName("tipo_objeto") val tipoObjeto: String? = null,
    val leida: Boolean,
    @SerialName("fecha_creacion") val fechaCreacion: String
)

@Serializable
data class NotificacionLeidaUpdate(
    val leida: Boolean
)
