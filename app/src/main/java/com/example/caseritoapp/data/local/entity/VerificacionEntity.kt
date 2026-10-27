package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Códigos de verificación temporal (email o teléfono) para registro o recuperación.
 */
@Entity(
    tableName = "verificaciones",
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
data class VerificacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long,

    val tipo: String, // 'email', 'telefono'

    val codigo: String,

    @ColumnInfo(name = "expira_en")
    val expiraEn: String,

    val usado: Boolean = false
)
