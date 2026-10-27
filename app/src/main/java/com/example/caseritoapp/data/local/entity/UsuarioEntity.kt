package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa a todos los usuarios del sistema (clientes, staff de tiendas y administradores).
 */
@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["telefono"], unique = true)
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "tipo_usuario")
    val tipoUsuario: String, // 'cliente', 'staff_tienda', 'admin'

    val nombre: String,

    val email: String? = null,

    val telefono: String? = null,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "ciudad_preferida")
    val ciudadPreferida: String? = null,

    val estado: String = "activo", // 'activo', 'bloqueado', 'eliminado'

    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: String,

    @ColumnInfo(name = "ultimo_login")
    val ultimoLogin: String? = null
)
