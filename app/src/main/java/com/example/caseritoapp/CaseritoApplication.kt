package com.example.caseritoapp

import android.app.Application
import com.example.caseritoapp.data.local.CaseritoDatabase

class CaseritoApplication : Application() {

    val database: CaseritoDatabase by lazy {
        CaseritoDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
