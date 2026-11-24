-- Alertas reales de Caserito.
-- 1) Avisa al dueño de una reserva cuando cambia su estado.
-- 2) Avisa a quienes guardaron una tienda cuando esta publica una oferta activa.

create or replace function public.notificar_cambio_estado_reserva()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_titulo text;
  v_mensaje text;
begin
  if new.estado is not distinct from old.estado then return new; end if;

  v_titulo := case new.estado
    when 'confirmada' then 'Tu reserva fue confirmada'
    when 'recogida' then 'Reserva recogida'
    when 'cancelada' then 'Tu reserva fue cancelada'
    when 'expirada' then 'Tu reserva expiró'
    else 'Actualización de tu reserva'
  end;
  v_mensaje := case new.estado
    when 'confirmada' then 'La tienda confirmó tu reserva. Ya puedes coordinar el recojo.'
    when 'recogida' then 'La reserva figura como recogida. ¡Gracias por usar Caserito!'
    when 'cancelada' then 'La reserva fue cancelada. Puedes revisar otras ofertas disponibles.'
    when 'expirada' then 'El plazo para recoger la reserva terminó.'
    else 'El estado de tu reserva cambió a ' || new.estado || '.'
  end;

  insert into public.notificaciones (id_usuario, tipo, titulo, mensaje, id_objeto, tipo_objeto, leida)
  values (new.id_usuario, 'estado_reserva', v_titulo, v_mensaje, new.id, 'reserva', false);
  return new;
end;
$$;

drop trigger if exists trigger_notificar_cambio_estado_reserva on public.reservas;
create trigger trigger_notificar_cambio_estado_reserva
after update of estado on public.reservas
for each row execute function public.notificar_cambio_estado_reserva();

create or replace function public.notificar_nueva_oferta_tienda()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_tienda_id bigint;
  v_nombre_tienda text;
begin
  if new.estado <> 'activo' or new.fecha_inicio > now() or new.fecha_fin <= now() then return new; end if;

  select u.id_tienda, t.nombre_comercial
    into v_tienda_id, v_nombre_tienda
  from public.ubicaciones u
  join public.tiendas t on t.id = u.id_tienda
  where u.id = new.id_ubicacion;

  if v_tienda_id is null then return new; end if;

  insert into public.notificaciones (id_usuario, tipo, titulo, mensaje, id_objeto, tipo_objeto, leida)
  select distinct f.id_usuario,
         'nueva_oferta_tienda',
         'Nueva oferta de ' || v_nombre_tienda,
         new.nombre || ' ahora está en oferta por S/ ' || new.precio_oferta || '.',
         new.id,
         'producto',
         false
  from public.favoritos f
  where f.tipo = 'tienda'
    and f.id_objeto = v_tienda_id
    and f.id_usuario is not null
    and f.id_usuario is distinct from (select t.id_cuenta_responsable from public.tiendas t where t.id = v_tienda_id);

  return new;
end;
$$;

drop trigger if exists trigger_notificar_nueva_oferta_tienda on public.productos_oferta;
create trigger trigger_notificar_nueva_oferta_tienda
after insert on public.productos_oferta
for each row execute function public.notificar_nueva_oferta_tienda();

create or replace function public.notificar_nueva_reserva_comerciante()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_nombre_producto text;
  v_nombre_sucursal text;
  v_tienda_id bigint;
begin
  select p.nombre, u.nombre_sucursal, u.id_tienda
    into v_nombre_producto, v_nombre_sucursal, v_tienda_id
  from public.productos_oferta p
  join public.ubicaciones u on u.id = p.id_ubicacion
  where p.id = new.id_producto and u.id = new.id_ubicacion;

  if v_tienda_id is null then return new; end if;

  insert into public.notificaciones (id_usuario, tipo, titulo, mensaje, id_objeto, tipo_objeto, leida)
  select distinct destinatarios.id_usuario,
         'nueva_reserva',
         'Nueva reserva en tu tienda',
         'Se reservó ' || new.cantidad || ' unidad(es) de ' || v_nombre_producto || ' en ' || v_nombre_sucursal || '.',
         new.id,
         'reserva',
         false
  from (
    select t.id_cuenta_responsable as id_usuario
    from public.tiendas t where t.id = v_tienda_id
    union
    select ut.id_usuario
    from public.usuarios_tienda ut where ut.id_tienda = v_tienda_id
  ) destinatarios
  where destinatarios.id_usuario is not null
    and destinatarios.id_usuario is distinct from new.id_usuario;

  return new;
end;
$$;

drop trigger if exists trigger_notificar_nueva_reserva_comerciante on public.reservas;
create trigger trigger_notificar_nueva_reserva_comerciante
after insert on public.reservas
for each row execute function public.notificar_nueva_reserva_comerciante();

revoke execute on function public.notificar_cambio_estado_reserva() from public, anon, authenticated;
revoke execute on function public.notificar_nueva_oferta_tienda() from public, anon, authenticated;
revoke execute on function public.notificar_nueva_reserva_comerciante() from public, anon, authenticated;
