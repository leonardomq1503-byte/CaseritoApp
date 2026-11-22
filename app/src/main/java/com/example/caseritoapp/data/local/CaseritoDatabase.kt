package com.example.caseritoapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.caseritoapp.data.local.dao.FavoritoDao
import com.example.caseritoapp.data.local.dao.NotificacionDao
import com.example.caseritoapp.data.local.dao.OfertaDao
import com.example.caseritoapp.data.local.dao.ReservaDao
import com.example.caseritoapp.data.local.dao.TiendaDao
import com.example.caseritoapp.data.local.dao.UsuarioDao
import com.example.caseritoapp.data.local.entity.CategoriaEntity
import com.example.caseritoapp.data.local.entity.DocumentoTiendaEntity
import com.example.caseritoapp.data.local.entity.FavoritoEntity
import com.example.caseritoapp.data.local.entity.HistorialOfertaEntity
import com.example.caseritoapp.data.local.entity.ImagenProductoEntity
import com.example.caseritoapp.data.local.entity.MetricaProductoEntity
import com.example.caseritoapp.data.local.entity.NotificacionEntity
import com.example.caseritoapp.data.local.entity.ProductoOfertaEntity
import com.example.caseritoapp.data.local.entity.ReservaEntity
import com.example.caseritoapp.data.local.entity.ResponsableUbicacionEntity
import com.example.caseritoapp.data.local.entity.SesionEntity
import com.example.caseritoapp.data.local.entity.TiendaEntity
import com.example.caseritoapp.data.local.entity.UbicacionEntity
import com.example.caseritoapp.data.local.entity.UsuarioEntity
import com.example.caseritoapp.data.local.entity.UsuarioTiendaEntity
import com.example.caseritoapp.data.local.entity.ValoracionEntity
import com.example.caseritoapp.data.local.entity.VerificacionEntity

@Database(
    entities = [
        UsuarioEntity::class,
        SesionEntity::class,
        VerificacionEntity::class,
        TiendaEntity::class,
        DocumentoTiendaEntity::class,
        UsuarioTiendaEntity::class,
        UbicacionEntity::class,
        ResponsableUbicacionEntity::class,
        CategoriaEntity::class,
        ProductoOfertaEntity::class,
        ImagenProductoEntity::class,
        FavoritoEntity::class,
        ReservaEntity::class,
        ValoracionEntity::class,
        NotificacionEntity::class,
        MetricaProductoEntity::class,
        HistorialOfertaEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CaseritoDatabase : RoomDatabase() {

    abstract fun ofertaDao(): OfertaDao
    abstract fun tiendaDao(): TiendaDao
    abstract fun favoritoDao(): FavoritoDao
    abstract fun notificacionDao(): NotificacionDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun reservaDao(): ReservaDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE usuarios ADD COLUMN auth_user_id TEXT")
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_usuarios_auth_user_id ON usuarios(auth_user_id)"
                )
            }
        }

        @Volatile
        private var INSTANCE: CaseritoDatabase? = null

        fun getDatabase(context: Context): CaseritoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CaseritoDatabase::class.java,
                    "caserito_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
