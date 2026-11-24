-- Cancela una reserva pendiente del usuario autenticado y devuelve las unidades
-- al stock dentro de la misma transacción.
create or replace function public.cancelar_reserva(p_id_reserva bigint)
returns table (
  reserva_id bigint,
  reserva_estado text,
  stock_restante integer,
  oferta_estado text
)
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_usuario uuid := auth.uid();
  v_reserva public.reservas%rowtype;
  v_stock integer;
  v_estado_oferta text;
begin
  if v_usuario is null then
    raise exception 'Debes iniciar sesión para cancelar una reserva';
  end if;

  select * into v_reserva
  from public.reservas r
  where r.id = p_id_reserva
  for update;

  if not found then
    raise exception 'No se encontró la reserva';
  end if;
  if v_reserva.id_usuario <> v_usuario then
    raise exception 'No tienes permiso para cancelar esta reserva';
  end if;
  if v_reserva.estado <> 'pendiente' then
    raise exception 'Solo se pueden cancelar reservas pendientes';
  end if;

  update public.reservas r
  set estado = 'cancelada'
  where r.id = v_reserva.id;

  update public.productos_oferta p
  set stock_disponible = p.stock_disponible + v_reserva.cantidad,
      estado = case
        when p.estado = 'agotado' and p.fecha_fin <= now() then 'expirado'
        when p.estado = 'agotado' and p.fecha_inicio <= now() and p.fecha_fin > now() then 'activo'
        else p.estado
      end
  where p.id = v_reserva.id_producto
  returning p.stock_disponible, p.estado into v_stock, v_estado_oferta;

  if not found then
    raise exception 'No se encontró el producto de la reserva';
  end if;

  return query select v_reserva.id, 'cancelada'::text, v_stock, v_estado_oferta;
end;
$$;

revoke execute on function public.cancelar_reserva(bigint) from public, anon;
grant execute on function public.cancelar_reserva(bigint) to authenticated;
