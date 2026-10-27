package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Documentos requeridos para validar legalmente una tienda.
 */
@Entity(
    tableName = "documentos_tienda",
    foreignKeys = [
        ForeignKey(
            entity = TiendaEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_tienda"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_tienda"])
    ]
)
data class DocumentoTiendaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_tienda")
    val idTienda: Long,

    @ColumnInfo(name = "tipo_documento")
    val tipoDocumento: String,

    @ColumnInfo(name = "archivo_url")
    val archivoUrl: String,

    val estado: String = "pendiente", // 'pendiente', 'aprobado', 'rechazado'

    val comentarios: String? = null,

    @ColumnInfo(name = "fecha_subida")
    val fechaSubida: String
)
