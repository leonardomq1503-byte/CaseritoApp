package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Categorías de productos con soporte para jerarquías y subcategorías.
 */
@Entity(
    tableName = "categorias",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_padre"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["id_padre"])
    ]
)
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nombre: String,

    @ColumnInfo(name = "id_padre")
    val idPadre: Long? = null
)
