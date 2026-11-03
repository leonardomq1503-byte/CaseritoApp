package com.example.caseritoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.caseritoapp.data.local.entity.SesionEntity
import com.example.caseritoapp.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun getUsuarioPorId(id: Long): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getUsuarioPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE telefono = :telefono LIMIT 1")
    suspend fun getUsuarioPorTelefono(telefono: String): UsuarioEntity?

    @Query("""
        SELECT u.* FROM usuarios u
        INNER JOIN sesiones s ON s.id_usuario = u.id
        ORDER BY s.id DESC LIMIT 1
    """)
    fun getUsuarioSesionActiva(): Flow<UsuarioEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSesion(sesion: SesionEntity): Long

    @Query("DELETE FROM sesiones WHERE id_usuario = :idUsuario")
    suspend fun cerrarSesion(idUsuario: Long)

    @Update
    suspend fun updateUsuario(usuario: UsuarioEntity)
}
