package com.example.caseritoapp.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.caseritoapp.data.local.entity.CategoriaEntity
import com.example.caseritoapp.data.local.entity.ImagenProductoEntity
import com.example.caseritoapp.data.local.entity.ProductoOfertaEntity
import kotlinx.coroutines.flow.Flow

data class OfertaConDetalle(
    @Embedded val oferta: ProductoOfertaEntity,
    @ColumnInfo(name = "nombre_tienda") val nombreTienda: String? = null,
    @ColumnInfo(name = "nombre_sucursal") val nombreSucursal: String? = null,
    @ColumnInfo(name = "nombre_categoria") val nombreCategoria: String? = null
)

@Dao
interface OfertaDao {

    @Query("""
        SELECT o.*, t.nombre_comercial AS nombre_tienda, u.nombre_sucursal AS nombre_sucursal, c.nombre AS nombre_categoria
        FROM productos_oferta o
        LEFT JOIN ubicaciones u ON o.id_ubicacion = u.id
        LEFT JOIN tiendas t ON u.id_tienda = t.id
        LEFT JOIN categorias c ON o.id_categoria = c.id
        WHERE o.estado = 'activo'
        ORDER BY o.fecha_inicio DESC
    """)
    fun getOfertasConDetalle(): Flow<List<OfertaConDetalle>>

    @Query("SELECT * FROM productos_oferta WHERE estado = 'activo' ORDER BY fecha_inicio DESC")
    fun getOfertasActivas(): Flow<List<ProductoOfertaEntity>>

    @Query("SELECT * FROM productos_oferta WHERE id_categoria = :categoriaId AND estado = 'activo'")
    fun getOfertasPorCategoria(categoriaId: Long): Flow<List<ProductoOfertaEntity>>

    @Query("""
        SELECT * FROM productos_oferta 
        WHERE (nombre LIKE '%' || :query || '%' OR descripcion LIKE '%' || :query || '%')
          AND estado = 'activo'
    """)
    fun buscarOfertas(query: String): Flow<List<ProductoOfertaEntity>>

    @Query("SELECT * FROM productos_oferta WHERE id = :id LIMIT 1")
    suspend fun getOfertaPorId(id: Long): ProductoOfertaEntity?

    @Query("SELECT * FROM imagenes_producto WHERE id_producto = :idProducto ORDER BY es_principal DESC, orden ASC")
    fun getImagenesPorProducto(idProducto: Long): Flow<List<ImagenProductoEntity>>

    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    fun getCategorias(): Flow<List<CategoriaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfertas(ofertas: List<ProductoOfertaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOferta(oferta: ProductoOfertaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategorias(categorias: List<CategoriaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImagenes(imagenes: List<ImagenProductoEntity>)

    @Update
    suspend fun updateOferta(oferta: ProductoOfertaEntity)

    @Delete
    suspend fun deleteOferta(oferta: ProductoOfertaEntity)
}
