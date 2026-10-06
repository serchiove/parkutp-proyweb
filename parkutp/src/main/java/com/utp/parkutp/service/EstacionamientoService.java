package com.utp.parkutp.service;
import com.utp.parkutp.dao.*;
import com.utp.parkutp.dto.EstacionamientoDto;
import com.utp.parkutp.exception.NegocioException;
import com.utp.parkutp.model.Estacionamiento;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class EstacionamientoService {
 private final EstacionamientoDao dao;
 private final SedeService sedes;
 public EstacionamientoService(EstacionamientoDao dao,SedeService sedes) { this.dao=dao;this.sedes=sedes; }
 public List<Estacionamiento> listar() { return dao.listar(); }
 public Estacionamiento buscar(long id) { return dao.buscar(id).orElseThrow(()->NegocioException.noEncontrado("El estacionamiento no existe")); }
 public void bloquear(long id) { if(!dao.bloquear(id)) throw NegocioException.noEncontrado("El estacionamiento no existe"); }
 @Transactional public Estacionamiento crear(EstacionamientoDto dto) {
  sedes.buscar(dto.getSedeId());
  try {
   long id=dao.insertar(dto.getSedeId(),dto.getNombre().strip(),dto.getCapacidad(),dto.isActivo());
   dao.crearAccesos(id); return buscar(id);
  } catch(DataIntegrityViolationException ex) { throw NegocioException.conflicto("Nombre duplicado en la sede o sede eliminada durante la operación"); }
 }
 @Transactional public Estacionamiento actualizar(long id,EstacionamientoDto dto) {
  bloquear(id); Estacionamiento actual=buscar(id); sedes.buscar(dto.getSedeId());
  if(dto.getCapacidad()<actual.ocupados()) throw NegocioException.conflicto("La capacidad no puede ser menor que los vehículos dentro");
  if(!actual.sedeId().equals(dto.getSedeId()) && dao.cantidadMovimientos(id)>0) throw NegocioException.conflicto("No se puede cambiar de sede un estacionamiento con historial");
  try { dao.actualizar(id,dto.getSedeId(),dto.getNombre().strip(),dto.getCapacidad(),dto.isActivo()); }
  catch(DataIntegrityViolationException ex) { throw NegocioException.conflicto("Nombre duplicado en la sede o sede eliminada durante la operación"); }
  return buscar(id);
 }
 @Transactional public void eliminar(long id) {
  bloquear(id);
  if(dao.cantidadMovimientos(id)>0) throw NegocioException.conflicto("Tiene historial de movimientos; desactívalo para conservarlo");
  try { dao.eliminar(id); }
  catch(DataIntegrityViolationException ex) { throw NegocioException.conflicto("Tiene dispositivos asociados; desactívalo para conservarlos"); }
 }
}
