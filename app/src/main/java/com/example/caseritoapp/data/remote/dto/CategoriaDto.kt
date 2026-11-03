package com.example.caseritoapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoriaDto(
    val id: Long,
    val nombre: String,
    @SerialName("id_padre")
    val idPadre: Long? = null
)
