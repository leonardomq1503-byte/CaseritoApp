package com.example.caseritoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_operations",
    indices = [Index(value = ["auth_user_id", "id"])]
)
data class SyncOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "auth_user_id")
    val authUserId: String,
    val entity: String,
    val action: String,
    val tipo: String? = null,
    @ColumnInfo(name = "id_objeto")
    val idObjeto: Long? = null,
    @ColumnInfo(name = "id_remoto")
    val idRemoto: Long? = null,
    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: String
)
