package com.example.caseritoapp

import android.app.Application
import com.example.caseritoapp.data.local.CaseritoDatabase
import com.example.caseritoapp.data.repository.AuthRepository
import com.example.caseritoapp.data.repository.FavoritoRepository
import com.example.caseritoapp.data.repository.NotificacionRepository
import com.example.caseritoapp.data.repository.OfertaRepository
import com.example.caseritoapp.data.repository.TiendaRepository
import com.example.caseritoapp.data.repository.PersonalDataSyncRepository
import com.example.caseritoapp.data.repository.ReservaRepository
import com.example.caseritoapp.data.repository.MerchantRepository
import com.example.caseritoapp.data.sync.SyncScheduler

class CaseritoApplication : Application() {

    val database: CaseritoDatabase by lazy {
        CaseritoDatabase.getDatabase(this)
    }

    val ofertaRepository: OfertaRepository by lazy {
        OfertaRepository(database.ofertaDao())
    }

    val tiendaRepository: TiendaRepository by lazy {
        TiendaRepository(database.tiendaDao())
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(database.usuarioDao())
    }

    val favoritoRepository: FavoritoRepository by lazy {
        FavoritoRepository(database, ::schedulePersonalDataSync)
    }

    val notificacionRepository: NotificacionRepository by lazy {
        NotificacionRepository(database, ::schedulePersonalDataSync)
    }

    val personalDataSyncRepository: PersonalDataSyncRepository by lazy {
        PersonalDataSyncRepository(database, onNewNotification = { notification ->
            com.example.caseritoapp.util.NotificationHelper.mostrarNotificacion(
                context = this,
                titulo = notification.titulo,
                mensaje = notification.mensaje.orEmpty(),
                notifId = notification.idRemoto?.hashCode() ?: notification.id.hashCode()
            )
        })
    }

    val reservaRepository: ReservaRepository by lazy {
        ReservaRepository(database)
    }

    val merchantRepository: MerchantRepository by lazy {
        MerchantRepository()
    }

    fun schedulePersonalDataSync(authUserId: String) {
        SyncScheduler.enqueue(this, authUserId)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
