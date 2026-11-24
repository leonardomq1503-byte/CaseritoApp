package com.example.caseritoapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MerchantStoreDto(
    val id: Long,
    @SerialName("nombre_comercial") val nombreComercial: String,
    val estado: String,
    @SerialName("fecha_registro") val fechaRegistro: String? = null
)

@Serializable
data class MerchantLocationDto(
    val id: Long,
    @SerialName("id_tienda") val idTienda: Long,
    @SerialName("nombre_sucursal") val nombreSucursal: String,
    val direccion: String,
    val ciudad: String
)

@Serializable
data class MerchantOfferDto(
    val id: Long,
    @SerialName("id_ubicacion") val idUbicacion: Long,
    val nombre: String,
    @SerialName("precio_original") val precioOriginal: Double,
    @SerialName("precio_oferta") val precioOferta: Double,
    @SerialName("stock_disponible") val stockDisponible: Int,
    @SerialName("fecha_fin") val fechaFin: String,
    val estado: String
)

@Serializable
data class MerchantOfferCreatedDto(@SerialName("id_producto") val idProducto: Long)
