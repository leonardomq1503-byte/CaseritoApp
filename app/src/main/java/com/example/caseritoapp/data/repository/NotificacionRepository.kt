package com.example.caseritoapp.data.repository

import com.example.caseritoapp.data.local.dao.NotificacionDao
import com.example.caseritoapp.data.local.dao.UsuarioDao
import com.example.caseritoapp.data.local.entity.NotificacionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificacionRepository(
    private val notificacionDao: NotificacionDao,
    private val usuarioDao: UsuarioDao
) {

    fun getNotificacionesPorUsuario(idUsuario: Long): Flow<List<NotificacionEntity>> {
        return notificacionDao.getNotificacionesPorUsuario(idUsuario)
    }

    fun getCantidadNoLeidas(idUsuario: Long): Flow<Int> {
        return notificacionDao.getCantidadNoLeidas(idUsuario)
    }

    suspend fun marcarComoLeida(id: Long, idUsuario: Long) = withContext(Dispatchers.IO) {
        notificacionDao.marcarComoLeida(id, idUsuario)
    }

    suspend fun marcarTodasComoLeidas(idUsuario: Long) = withContext(Dispatchers.IO) {
        notificacionDao.marcarTodasComoLeidas(idUsuario)
    }

    suspend fun agregarNotificacion(
        idUsuario: Long,
        titulo: String,
        mensaje: String,
        tipo: String = "nueva_oferta_tienda",
        idObjeto: Long? = null,
        tipoObjeto: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        notificacionDao.insertNotificacion(
            NotificacionEntity(
                idUsuario = idUsuario,
                tipo = tipo,
                titulo = titulo,
                mensaje = mensaje,
                idObjeto = idObjeto,
                tipoObjeto = tipoObjeto,
                leida = false,
                fechaCreacion = fechaActual
            )
        )
    }

    suspend fun inicializarNotificacionesSiVacio(idUsuario: Long) = withContext(Dispatchers.IO) {
        val existentes = notificacionDao.getNotificacionesPorUsuario(idUsuario).firstOrNull()
        if (existentes.isNullOrEmpty()) {
            val fechaActual = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            val semillas = listOf(
                NotificacionEntity(
                    idUsuario = idUsuario,
                    tipo = "nueva_oferta_tienda",
                    titulo = "¡Nueva oferta en TecnoStore!",
                    mensaje = "Audífonos Bluetooth Pro con 44% de descuento hasta el fin de semana.",
                    idObjeto = 1L,
                    tipoObjeto = "producto",
                    leida = false,
                    fechaCreacion = fechaActual
                ),
                NotificacionEntity(
                    idUsuario = idUsuario,
                    tipo = "estado_reserva",
                    titulo = "Reserva lista para retiro",
                    mensaje = "Tu reserva en Moda & Estilo está confirmada. Tienes 48h para retirarla en tienda.",
                    idObjeto = 2L,
                    tipoObjeto = "tienda",
                    leida = false,
                    fechaCreacion = fechaActual
                ),
                NotificacionEntity(
                    idUsuario = idUsuario,
                    tipo = "cercania",
                    titulo = "Tienda favorita cerca de ti",
                    mensaje = "Estás a menos de 500m de Bazar Don Pepe con promociones vigentes en hogar.",
                    idObjeto = 3L,
                    tipoObjeto = "tienda",
                    leida = true,
                    fechaCreacion = fechaActual
                )
            )
            notificacionDao.insertNotificaciones(semillas)
        }
    }
}
