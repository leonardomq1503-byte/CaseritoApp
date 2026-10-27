package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla central de ofertas: producto promocional asociado a una sucursal con precio, descuento y stock.
 */
@Entity(
    tableName = "productos_oferta",
    foreignKeys = [
        ForeignKey(
            entity = UbicacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_ubicacion"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_categoria"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["id_ubicacion"]),
        Index(value = ["id_categoria"])
    ]
)
data class ProductoOfertaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_ubicacion")
    val idUbicacion: Long,

    val sku: String? = null,

    val nombre: String,

    val descripcion: String? = null,

    @ColumnInfo(name = "id_categoria")
    val idCategoria: Long? = null,

    @ColumnInfo(name = "precio_original")
    val precioOriginal: Double,

    @ColumnInfo(name = "precio_oferta")
    val precioOferta: Double,

    @ColumnInfo(name = "tipo_descuento")
    val tipoDescuento: String = "monto", // 'monto', 'porcentaje'

    @ColumnInfo(name = "stock_disponible")
    val stockDisponible: Int = 0,

    @ColumnInfo(name = "fecha_inicio")
    val fechaInicio: String,

    @ColumnInfo(name = "fecha_fin")
    val fechaFin: String,

    val estado: String = "activo", // 'activo', 'agotado', 'expirado', 'pausado'

    val condiciones: String? = null,

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: String
)
