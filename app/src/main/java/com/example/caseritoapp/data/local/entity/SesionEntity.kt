package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tokens de sesiones activas por usuario.
 */
@Entity(
    tableName = "sesiones",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["token"], unique = true),
        Index(value = ["id_usuario"])
    ]
)
data class SesionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long,

    val token: String,

    @ColumnInfo(name = "expira_en")
    val expiraEn: String
)
