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
import com.example.caseritoapp.data.local.dao.SyncOperationDao
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
import com.example.caseritoapp.data.local.entity.SyncOperationEntity
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
        HistorialOfertaEntity::class,
        SyncOperationEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CaseritoDatabase : RoomDatabase() {

    abstract fun ofertaDao(): OfertaDao
    abstract fun tiendaDao(): TiendaDao
    abstract fun favoritoDao(): FavoritoDao
    abstract fun notificacionDao(): NotificacionDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun reservaDao(): ReservaDao
    abstract fun syncOperationDao(): SyncOperationDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE usuarios ADD COLUMN auth_user_id TEXT")
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_usuarios_auth_user_id ON usuarios(auth_user_id)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notificaciones ADD COLUMN id_remoto INTEGER")
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_notificaciones_id_remoto ON notificaciones(id_remoto)"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS sync_operations (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, auth_user_id TEXT NOT NULL, entity TEXT NOT NULL, action TEXT NOT NULL, tipo TEXT, id_objeto INTEGER, id_remoto INTEGER, fecha_creacion TEXT NOT NULL)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_sync_operations_auth_user_id_id ON sync_operations(auth_user_id, id)"
                )
                database.execSQL("UPDATE favoritos SET tipo = 'producto' WHERE tipo = 'oferta'")
                database.execSQL(
                    "INSERT INTO sync_operations (auth_user_id, entity, action, tipo, id_objeto, fecha_creacion) " +
                        "SELECT u.auth_user_id, 'favorito', 'upsert', f.tipo, f.id_objeto, f.fecha_creacion " +
                        "FROM favoritos f INNER JOIN usuarios u ON u.id = f.id_usuario " +
                        "WHERE u.auth_user_id IS NOT NULL"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE reservas ADD COLUMN id_remoto INTEGER")
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_reservas_id_remoto ON reservas(id_remoto)"
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
