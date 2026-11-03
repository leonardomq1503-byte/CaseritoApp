package com.example.caseritoapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TiendaDto(
    val id: Long,
    @SerialName("id_cuenta_responsable")
    val idCuentaResponsable: String? = null,
    @SerialName("nombre_comercial")
    val nombreComercial: String,
    @SerialName("razon_social")
    val razonSocial: String? = null,
    @SerialName("tipo_negocio")
    val tipoNegocio: String? = null,
    @SerialName("categoria_principal")
    val categoriaPrincipal: String? = null,
    @SerialName("logo_url")
    val logoUrl: String? = null,
    val descripcion: String? = null,
    val web: String? = null,
    val estado: String = "verificada",
    @SerialName("fecha_registro")
    val fechaRegistro: String? = null
)
