package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Elementos favoritos guardados por el usuario (productos, tiendas o sucursales).
 */
@Entity(
    tableName = "favoritos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_usuario", "tipo", "id_objeto"], unique = true)
    ]
)
data class FavoritoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long,

    val tipo: String, // 'producto', 'tienda', 'ubicacion'

    @ColumnInfo(name = "id_objeto")
    val idObjeto: Long,

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: String
)
