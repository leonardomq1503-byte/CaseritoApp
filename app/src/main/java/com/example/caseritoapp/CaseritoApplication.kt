package com.example.caseritoapp

import android.app.Application
import com.example.caseritoapp.data.local.CaseritoDatabase
import com.example.caseritoapp.data.repository.AuthRepository
import com.example.caseritoapp.data.repository.FavoritoRepository
import com.example.caseritoapp.data.repository.OfertaRepository
import com.example.caseritoapp.data.repository.TiendaRepository

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
        FavoritoRepository(database.favoritoDao(), database.usuarioDao())
    }

    override fun onCreate() {
        super.onCreate()
    }
}
