package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Auditoría de cambios sobre campos clave de ofertas (precio, stock, estado, vigencia).
 */
@Entity(
    tableName = "historial_ofertas",
    foreignKeys = [
        ForeignKey(
            entity = ProductoOfertaEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_producto"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_usuario_cambio"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["id_producto"]),
        Index(value = ["id_usuario_cambio"])
    ]
)
data class HistorialOfertaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_producto")
    val idProducto: Long,

    val campo: String,

    @ColumnInfo(name = "valor_anterior")
    val valorAnterior: String? = null,

    @ColumnInfo(name = "valor_nuevo")
    val valorNuevo: String? = null,

    @ColumnInfo(name = "fecha_cambio")
    val fechaCambio: String,

    @ColumnInfo(name = "id_usuario_cambio")
    val idUsuarioCambio: Long? = null
)
