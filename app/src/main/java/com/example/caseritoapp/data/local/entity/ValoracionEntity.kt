package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Puntuaciones (1 a 5 estrellas) y comentarios sobre tiendas, sucursales o productos.
 */
@Entity(
    tableName = "valoraciones",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_usuario"]),
        Index(value = ["tipo", "id_objeto"])
    ]
)
data class ValoracionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long,

    val tipo: String, // 'tienda', 'ubicacion', 'producto'

    @ColumnInfo(name = "id_objeto")
    val idObjeto: Long,

    val puntuacion: Int, // 1 a 5

    val comentario: String? = null,

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: String
)
