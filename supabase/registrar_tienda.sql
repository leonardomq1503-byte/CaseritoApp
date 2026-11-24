create or replace function public.registrar_tienda(
  p_nombre_comercial text,
  p_nombre_sucursal text,
  p_direccion text,
  p_ciudad text,
  p_razon_social text,
  p_tipo_negocio text,
  p_categoria_principal text,
  p_descripcion text,
  p_web text,
  p_telefono text,
  p_horarios text,
  p_latitud double precision,
  p_longitud double precision
)
returns table (id_tienda bigint, id_ubicacion bigint)
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_usuario uuid := auth.uid();
  v_id_tienda bigint;
  v_id_ubicacion bigint;
begin
  if v_usuario is null then
    raise exception 'Debes iniciar sesión para registrar una tienda';
  end if;

  if nullif(trim(p_nombre_comercial), '') is null
     or nullif(trim(p_nombre_sucursal), '') is null
     or nullif(trim(p_direccion), '') is null
     or nullif(trim(p_ciudad), '') is null then
    raise exception 'Completa el nombre comercial, la sucursal, la dirección y la ciudad';
  end if;

  if (p_latitud is null) <> (p_longitud is null) then
    raise exception 'Ingresa ambas coordenadas o deja las dos vacías';
  end if;

  if p_latitud is not null and (p_latitud < -90 or p_latitud > 90) then
    raise exception 'La latitud debe estar entre -90 y 90';
  end if;

  if p_longitud is not null and (p_longitud < -180 or p_longitud > 180) then
    raise exception 'La longitud debe estar entre -180 y 180';
  end if;

  if not exists (select 1 from public.usuarios u where u.id = v_usuario) then
    raise exception 'No se encontró el perfil asociado a tu cuenta';
  end if;

  update public.usuarios
  set tipo_usuario = 'staff_tienda'
  where id = v_usuario and tipo_usuario = 'cliente';

  insert into public.tiendas (
    id_cuenta_responsable,
    nombre_comercial,
    razon_social,
    tipo_negocio,
    categoria_principal,
    descripcion,
    web,
    estado
  ) values (
    v_usuario,
    trim(p_nombre_comercial),
    nullif(trim(p_razon_social), ''),
    nullif(trim(p_tipo_negocio), ''),
    nullif(trim(p_categoria_principal), ''),
    nullif(trim(p_descripcion), ''),
    nullif(trim(p_web), ''),
    'pendiente'
  ) returning id into v_id_tienda;

  insert into public.ubicaciones (
    id_tienda,
    nombre_sucursal,
    direccion,
    ciudad,
    pais,
    latitud,
    longitud,
    horarios,
    telefono,
    estado
  ) values (
    v_id_tienda,
    trim(p_nombre_sucursal),
    trim(p_direccion),
    trim(p_ciudad),
    'Perú',
    p_latitud,
    p_longitud,
    nullif(trim(p_horarios), ''),
    nullif(trim(p_telefono), ''),
    'activa'
  ) returning id into v_id_ubicacion;

  return query select v_id_tienda, v_id_ubicacion;
end;
$$;

revoke execute on function public.registrar_tienda(
  text, text, text, text, text, text, text, text, text, text, text, double precision, double precision
) from public, anon;

grant execute on function public.registrar_tienda(
  text, text, text, text, text, text, text, text, text, text, text, double precision, double precision
) to authenticated;
