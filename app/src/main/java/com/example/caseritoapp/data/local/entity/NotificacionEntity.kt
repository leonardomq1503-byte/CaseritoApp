package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Buzón de notificaciones de usuario (nuevas ofertas de tiendas seguidas, estados de reservas, etc.).
 */
@Entity(
    tableName = "notificaciones",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_usuario"])
    ]
)
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long,

    val tipo: String, // 'nueva_oferta_tienda', 'estado_reserva', 'recordatorio', etc.

    val titulo: String,

    val mensaje: String? = null,

    @ColumnInfo(name = "id_objeto")
    val idObjeto: Long? = null,

    @ColumnInfo(name = "tipo_objeto")
    val tipoObjeto: String? = null, // 'tienda', 'producto', 'ubicacion'

    val leida: Boolean = false,

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: String
)
