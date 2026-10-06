package com.utp.parkutp.service;
import com.utp.parkutp.dao.*;
import com.utp.parkutp.dto.MovimientoDto;
import com.utp.parkutp.exception.NegocioException;
import com.utp.parkutp.model.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class MovimientoService {
 private final EstacionamientoService estacionamientos;
 private final EstacionamientoDao accesos;
 private final MovimientoDao dao;
 public MovimientoService(EstacionamientoService estacionamientos,EstacionamientoDao accesos,MovimientoDao dao) { this.estacionamientos=estacionamientos;this.accesos=accesos;this.dao=dao; }
 public List<Movimiento> recientes(long id) { estacionamientos.buscar(id);return dao.recientes(id); }
 @Transactional public Movimiento registrar(long id,MovimientoDto dto) {
  estacionamientos.bloquear(id);
  String placa=dto.getPlaca()==null?null:dto.getPlaca().toUpperCase(Locale.ROOT);
  Optional<Movimiento> previo=dao.buscarEvento(dto.getEventoId());
  if(previo.isPresent()) {
   Movimiento m=previo.get();
   if(m.estacionamientoId()!=id || !m.tipo().equals(dto.getTipo()) || !m.origen().equals(dto.getOrigen()) || !Objects.equals(m.placa(),placa)) throw NegocioException.conflicto("El evento ya fue usado con otros datos");
   return m;
  }
  Estacionamiento e=estacionamientos.buscar(id);
  if("ENTRADA".equals(dto.getTipo())) {
   if(!e.activo()) throw NegocioException.conflicto("El estacionamiento está inactivo");
   if(e.getDisponibles()<=0) throw NegocioException.conflicto("El estacionamiento está lleno");
  } else if(e.ocupados()<=0) throw NegocioException.conflicto("No hay vehículos dentro para registrar una salida");
  try { return dao.insertar(dto.getEventoId(),id,accesos.acceso(id,dto.getTipo()),dto.getTipo(),dto.getOrigen(),placa); }
  catch(DuplicateKeyException ex) { throw NegocioException.conflicto("El identificador del evento ya existe; comprueba los datos"); }
 }
}
