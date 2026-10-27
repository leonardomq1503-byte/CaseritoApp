package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Sucursales físicas de cada tienda con geolocalización y datos de contacto.
 */
@Entity(
    tableName = "ubicaciones",
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
data class UbicacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_tienda")
    val idTienda: Long,

    @ColumnInfo(name = "nombre_sucursal")
    val nombreSucursal: String,

    val direccion: String,

    val ciudad: String,

    val pais: String,

    val latitud: Double? = null,

    val longitud: Double? = null,

    val horarios: String? = null,

    val telefono: String? = null,

    val estado: String = "activa", // 'activa', 'cerrada_temporal', 'cerrada', 'solo_recogida'

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: String
)
