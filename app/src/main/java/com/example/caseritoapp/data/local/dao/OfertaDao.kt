package com.example.caseritoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.caseritoapp.data.local.entity.CategoriaEntity
import com.example.caseritoapp.data.local.entity.ImagenProductoEntity
import com.example.caseritoapp.data.local.entity.ProductoOfertaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfertaDao {

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
