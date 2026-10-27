package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Vinculación de usuarios encargados de una sucursal física determinada.
 */
@Entity(
    tableName = "responsable_ubicacion",
    foreignKeys = [
        ForeignKey(
            entity = UbicacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_ubicacion"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_ubicacion", "id_usuario"], unique = true),
        Index(value = ["id_usuario"])
    ]
)
data class ResponsableUbicacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_ubicacion")
    val idUbicacion: Long,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long
)
