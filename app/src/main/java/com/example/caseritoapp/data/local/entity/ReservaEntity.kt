package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Reservas de productos realizadas por los clientes antes de retirarlos en tienda.
 */
@Entity(
    tableName = "reservas",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE
        ),
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
        Index(value = ["id_usuario"]),
        Index(value = ["id_producto"]),
        Index(value = ["id_ubicacion"])
    ]
)
data class ReservaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long,

    @ColumnInfo(name = "id_producto")
    val idProducto: Long,

    @ColumnInfo(name = "id_ubicacion")
    val idUbicacion: Long,

    val cantidad: Int = 1,

    val estado: String = "pendiente", // 'pendiente', 'confirmada', 'recogida', 'cancelada', 'expirada'

    @ColumnInfo(name = "fecha_reserva")
    val fechaReserva: String,

    @ColumnInfo(name = "fecha_expiracion")
    val fechaExpiracion: String? = null
)
