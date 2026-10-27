package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tiendas o empresas comerciales registradas en la plataforma.
 */
@Entity(
    tableName = "tiendas",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_cuenta_responsable"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["id_cuenta_responsable"])
    ]
)
data class TiendaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "id_cuenta_responsable")
    val idCuentaResponsable: Long,

    @ColumnInfo(name = "nombre_comercial")
    val nombreComercial: String,

    @ColumnInfo(name = "razon_social")
    val razonSocial: String? = null,

    @ColumnInfo(name = "tipo_negocio")
    val tipoNegocio: String? = null,

    @ColumnInfo(name = "categoria_principal")
    val categoriaPrincipal: String? = null,

    @ColumnInfo(name = "logo_url")
    val logoUrl: String? = null,

    val descripcion: String? = null,

    val web: String? = null,

    val estado: String = "pendiente", // 'pendiente', 'verificada', 'rechazada', 'suspendida'

    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: String
)
