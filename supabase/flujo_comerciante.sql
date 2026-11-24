-- Panel del comerciante: consultar solicitudes, sucursales y ofertas propias,
-- publicar ofertas solo en tiendas verificadas y pausar/reactivar las propias.

create or replace function public.listar_mis_tiendas()
returns table (id bigint, nombre_comercial text, estado text, fecha_registro timestamptz)
language sql stable security definer set search_path = ''
as $$
  select t.id, t.nombre_comercial, t.estado, t.fecha_registro
  from public.tiendas t
  where t.id_cuenta_responsable = auth.uid()
  order by t.fecha_registro desc;
$$;

create or replace function public.listar_mis_ubicaciones()
returns table (id bigint, id_tienda bigint, nombre_sucursal text, direccion text, ciudad text)
language sql stable security definer set search_path = ''
as $$
  select u.id, u.id_tienda, u.nombre_sucursal, u.direccion, u.ciudad
  from public.ubicaciones u
  join public.tiendas t on t.id = u.id_tienda
  where t.id_cuenta_responsable = auth.uid()
  order by t.nombre_comercial, u.nombre_sucursal;
$$;

create or replace function public.listar_mis_ofertas()
returns table (
  id bigint,
  id_ubicacion bigint,
  nombre text,
  precio_original numeric,
  precio_oferta numeric,
  stock_disponible integer,
  fecha_fin timestamptz,
  estado text
)
language sql stable security definer set search_path = ''
as $$
  select p.id, p.id_ubicacion, p.nombre, p.precio_original, p.precio_oferta,
         p.stock_disponible, p.fecha_fin, p.estado
  from public.productos_oferta p
  join public.ubicaciones u on u.id = p.id_ubicacion
  join public.tiendas t on t.id = u.id_tienda
  where t.id_cuenta_responsable = auth.uid()
  order by p.fecha_creacion desc;
$$;

create or replace function public.crear_oferta_tienda(
  p_id_ubicacion bigint,
  p_nombre text,
  p_descripcion text,
  p_precio_original numeric,
  p_precio_oferta numeric,
  p_stock_disponible integer,
  p_fecha_fin timestamptz
)
returns table (id_producto bigint)
language plpgsql security definer set search_path = ''
as $$
declare
  v_id bigint;
begin
  if auth.uid() is null then raise exception 'Debes iniciar sesión'; end if;
  if nullif(trim(p_nombre), '') is null then raise exception 'El nombre del producto es obligatorio'; end if;
  if p_precio_original <= 0 or p_precio_oferta <= 0 or p_precio_oferta >= p_precio_original then
    raise exception 'El precio de oferta debe ser mayor que cero y menor que el precio regular';
  end if;
  if p_stock_disponible < 0 then raise exception 'El stock no puede ser negativo'; end if;
  if p_fecha_fin <= now() then raise exception 'La fecha de fin debe ser futura'; end if;
  if not exists (
    select 1 from public.ubicaciones u
    join public.tiendas t on t.id = u.id_tienda
    where u.id = p_id_ubicacion and u.estado = 'activa'
      and t.id_cuenta_responsable = auth.uid() and t.estado = 'verificada'
  ) then
    raise exception 'Solo puedes publicar ofertas en sucursales activas de una tienda verificada que administras';
  end if;

  insert into public.productos_oferta (
    id_ubicacion, nombre, descripcion, precio_original, precio_oferta,
    stock_disponible, fecha_inicio, fecha_fin, estado
  ) values (
    p_id_ubicacion, trim(p_nombre), nullif(trim(p_descripcion), ''),
    p_precio_original, p_precio_oferta, p_stock_disponible, now(), p_fecha_fin,
    case when p_stock_disponible = 0 then 'agotado' else 'activo' end
  ) returning id into v_id;

  return query select v_id;
end;
$$;

create or replace function public.cambiar_estado_oferta_tienda(
  p_id_producto bigint,
  p_nuevo_estado text
)
returns void
language plpgsql security definer set search_path = ''
as $$
begin
  if auth.uid() is null then raise exception 'Debes iniciar sesión'; end if;
  if p_nuevo_estado not in ('activo', 'pausado') then
    raise exception 'Estado de oferta no permitido';
  end if;
  update public.productos_oferta p
  set estado = p_nuevo_estado
  where p.id = p_id_producto
    and p.estado in ('activo', 'pausado')
    and p.stock_disponible > 0
    and p.fecha_fin > now()
    and exists (
      select 1 from public.ubicaciones u
      join public.tiendas t on t.id = u.id_tienda
      where u.id = p.id_ubicacion and u.estado = 'activa'
        and t.id_cuenta_responsable = auth.uid() and t.estado = 'verificada'
    );
  if not found then raise exception 'No puedes cambiar esta oferta; revisa su estado o tus permisos'; end if;
end;
$$;

revoke execute on function public.listar_mis_tiendas() from public, anon;
revoke execute on function public.listar_mis_ubicaciones() from public, anon;
revoke execute on function public.listar_mis_ofertas() from public, anon;
revoke execute on function public.crear_oferta_tienda(bigint, text, text, numeric, numeric, integer, timestamptz) from public, anon;
revoke execute on function public.cambiar_estado_oferta_tienda(bigint, text) from public, anon;
grant execute on function public.listar_mis_tiendas() to authenticated;
grant execute on function public.listar_mis_ubicaciones() to authenticated;
grant execute on function public.listar_mis_ofertas() to authenticated;
grant execute on function public.crear_oferta_tienda(bigint, text, text, numeric, numeric, integer, timestamptz) to authenticated;
grant execute on function public.cambiar_estado_oferta_tienda(bigint, text) to authenticated;
