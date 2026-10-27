package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Miembros del personal asignados a una tienda con roles específicos.
 */
@Entity(
    tableName = "usuarios_tienda",
    foreignKeys = [
        ForeignKey(
            entity = TiendaEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_tienda"],
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
        Index(value = ["id_tienda", "id_usuario"], unique = true),
        Index(value = ["id_usuario"])
    ]
)
data class UsuarioTiendaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_tienda")
    val idTienda: Long,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long,

    val rol: String // 'admin', 'editor_catalogo', 'solo_lectura'
)
