package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Métricas agregadas de visualizaciones y reservas por oferta y sucursal.
 */
@Entity(
    tableName = "metricas_producto",
    foreignKeys = [
        ForeignKey(
            entity = ProductoOfertaEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_producto"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UbicacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_ubicacion"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_producto"]),
        Index(value = ["id_ubicacion"])
    ]
)
data class MetricaProductoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_producto")
    val idProducto: Long,

    @ColumnInfo(name = "id_ubicacion")
    val idUbicacion: Long,

    val vistas: Int = 0,

    val reservas: Int = 0,

    val fecha: String
)
