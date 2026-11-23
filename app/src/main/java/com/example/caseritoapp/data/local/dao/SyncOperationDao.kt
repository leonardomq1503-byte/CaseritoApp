package com.example.caseritoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.caseritoapp.data.local.entity.SyncOperationEntity

@Dao
interface SyncOperationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(operation: SyncOperationEntity): Long

    @Query("SELECT * FROM sync_operations WHERE auth_user_id = :authUserId ORDER BY id ASC")
    suspend fun pendientes(authUserId: String): List<SyncOperationEntity>

    @Query("DELETE FROM sync_operations WHERE id = :id")
    suspend fun eliminar(id: Long)
}
