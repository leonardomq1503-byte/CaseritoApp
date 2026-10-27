package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Galería de imágenes vinculadas a cada producto en oferta.
 */
@Entity(
    tableName = "imagenes_producto",
    foreignKeys = [
        ForeignKey(
            entity = ProductoOfertaEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_producto"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_producto"])
    ]
)
data class ImagenProductoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_producto")
    val idProducto: Long,

    val url: String,

    @ColumnInfo(name = "es_principal")
    val esPrincipal: Boolean = false,

    val orden: Int = 0
)
