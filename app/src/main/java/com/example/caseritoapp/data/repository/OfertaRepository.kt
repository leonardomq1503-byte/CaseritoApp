package com.example.caseritoapp.data.repository

import com.example.caseritoapp.data.local.dao.OfertaDao
import com.example.caseritoapp.data.local.entity.CategoriaEntity
import com.example.caseritoapp.data.local.entity.ProductoOfertaEntity
import com.example.caseritoapp.data.mapper.toEntity
import com.example.caseritoapp.data.remote.SupabaseClientProvider
import com.example.caseritoapp.data.remote.dto.CategoriaDto
import com.example.caseritoapp.data.remote.dto.ProductoOfertaDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class OfertaRepository(
    private val ofertaDao: OfertaDao,
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) {
    // Single Source of Truth: La UI siempre observa desde Room
    val ofertasActivas: Flow<List<ProductoOfertaEntity>> = ofertaDao.getOfertasActivas()
    val categorias: Flow<List<CategoriaEntity>> = ofertaDao.getCategorias()

    fun getOfertasPorCategoria(categoriaId: Long): Flow<List<ProductoOfertaEntity>> {
        return ofertaDao.getOfertasPorCategoria(categoriaId)
    }

    fun buscarOfertas(query: String): Flow<List<ProductoOfertaEntity>> {
        return ofertaDao.buscarOfertas(query)
    }

    suspend fun getOfertaPorId(id: Long): ProductoOfertaEntity? {
        return ofertaDao.getOfertaPorId(id)
    }

    // Sincronización en segundo plano con Supabase
    suspend fun syncOfertas(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Descargar categorías
            val remoteCategorias = supabase.from("categorias")
                .select()
                .decodeList<CategoriaDto>()
            if (remoteCategorias.isNotEmpty()) {
                ofertaDao.insertCategorias(remoteCategorias.map { it.toEntity() })
            }

            // 2. Descargar ofertas activas
            val remoteOfertas = supabase.from("productos_oferta")
                .select()
                .decodeList<ProductoOfertaDto>()
            if (remoteOfertas.isNotEmpty()) {
                ofertaDao.insertOfertas(remoteOfertas.map { it.toEntity() })
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
