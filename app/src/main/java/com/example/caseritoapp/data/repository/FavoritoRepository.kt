package com.example.caseritoapp.data.repository

import com.example.caseritoapp.data.local.dao.FavoritoDao
import com.example.caseritoapp.data.local.dao.UsuarioDao
import com.example.caseritoapp.data.local.entity.FavoritoEntity
import com.example.caseritoapp.data.local.entity.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FavoritoRepository(
    private val favoritoDao: FavoritoDao,
    private val usuarioDao: UsuarioDao
) {
    suspend fun getOrCreateDefaultUserId(): Long = withContext(Dispatchers.IO) {
        val usuario = usuarioDao.getUsuarioPorEmail("invitado@caserito.pe")
        if (usuario != null) {
            usuario.id
        } else {
            val fechaActual = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            usuarioDao.insertUsuario(
                UsuarioEntity(
                    tipoUsuario = "cliente",
                    nombre = "Usuario Caserito",
                    email = "invitado@caserito.pe",
                    telefono = "999999999",
                    passwordHash = "",
                    fechaRegistro = fechaActual
                )
            )
        }
    }

    fun getFavoritosPorUsuario(idUsuario: Long): Flow<List<FavoritoEntity>> {
        return favoritoDao.getFavoritosPorUsuario(idUsuario)
    }

    fun getIdsFavoritos(idUsuario: Long, tipo: String): Flow<List<Long>> {
        return favoritoDao.getIdsFavoritos(idUsuario, tipo)
    }

    fun contarFavoritos(idUsuario: Long): Flow<Int> {
        return favoritoDao.contarFavoritos(idUsuario)
    }

    suspend fun toggleFavorito(idUsuario: Long, tipo: String, idObjeto: Long): Boolean = withContext(Dispatchers.IO) {
        val existente = favoritoDao.getFavorito(idUsuario, tipo, idObjeto)
        if (existente != null) {
            favoritoDao.deleteFavorito(idUsuario, tipo, idObjeto)
            false
        } else {
            val fechaActual = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            favoritoDao.insertFavorito(
                FavoritoEntity(
                    idUsuario = idUsuario,
                    tipo = tipo,
                    idObjeto = idObjeto,
                    fechaCreacion = fechaActual
                )
            )
            true
        }
    }
}
