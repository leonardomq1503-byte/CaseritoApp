package com.example.caseritoapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductoOfertaDto(
    val id: Long,
    @SerialName("id_ubicacion")
    val idUbicacion: Long,
    val sku: String? = null,
    val nombre: String,
    val descripcion: String? = null,
    @SerialName("id_categoria")
    val idCategoria: Long? = null,
    @SerialName("precio_original")
    val precioOriginal: Double,
    @SerialName("precio_oferta")
    val precioOferta: Double,
    @SerialName("tipo_descuento")
    val tipoDescuento: String = "monto",
    @SerialName("stock_disponible")
    val stockDisponible: Int = 0,
    @SerialName("fecha_inicio")
    val fechaInicio: String,
    @SerialName("fecha_fin")
    val fechaFin: String,
    val estado: String = "activo",
    val condiciones: String? = null,
    @SerialName("fecha_creacion")
    val fechaCreacion: String? = null
)
