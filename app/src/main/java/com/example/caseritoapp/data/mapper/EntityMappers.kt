package com.example.caseritoapp.data.mapper

import com.example.caseritoapp.data.local.entity.CategoriaEntity
import com.example.caseritoapp.data.local.entity.ProductoOfertaEntity
import com.example.caseritoapp.data.local.entity.TiendaEntity
import com.example.caseritoapp.data.local.entity.UbicacionEntity
import com.example.caseritoapp.data.remote.dto.CategoriaDto
import com.example.caseritoapp.data.remote.dto.ProductoOfertaDto
import com.example.caseritoapp.data.remote.dto.TiendaDto
import com.example.caseritoapp.data.remote.dto.UbicacionDto

fun CategoriaDto.toEntity(): CategoriaEntity = CategoriaEntity(
    id = id,
    nombre = nombre,
    idPadre = idPadre
)

fun TiendaDto.toEntity(): TiendaEntity = TiendaEntity(
    id = id,
    idCuentaResponsable = null,
    nombreComercial = nombreComercial,
    razonSocial = razonSocial,
    tipoNegocio = tipoNegocio,
    categoriaPrincipal = categoriaPrincipal,
    logoUrl = logoUrl,
    descripcion = descripcion,
    web = web,
    estado = estado,
    fechaRegistro = fechaRegistro ?: ""
)

fun UbicacionDto.toEntity(): UbicacionEntity = UbicacionEntity(
    id = id,
    idTienda = idTienda,
    nombreSucursal = nombreSucursal,
    direccion = direccion,
    ciudad = ciudad,
    pais = pais,
    latitud = latitud,
    longitud = longitud,
    horarios = horarios,
    telefono = telefono,
    estado = estado,
    fechaCreacion = fechaCreacion ?: ""
)

fun ProductoOfertaDto.toEntity(): ProductoOfertaEntity = ProductoOfertaEntity(
    id = id,
    idUbicacion = idUbicacion,
    sku = sku,
    nombre = nombre,
    descripcion = descripcion,
    idCategoria = idCategoria,
    precioOriginal = precioOriginal,
    precioOferta = precioOferta,
    tipoDescuento = tipoDescuento,
    stockDisponible = stockDisponible,
    fechaInicio = fechaInicio,
    fechaFin = fechaFin,
    estado = estado,
    condiciones = condiciones,
    fechaCreacion = fechaCreacion ?: ""
)
