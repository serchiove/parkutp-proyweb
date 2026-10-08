package com.utp.parkutp.service;
import com.utp.parkutp.dto.AccesoDto;
import com.utp.parkutp.entity.AccesoEntity;
import com.utp.parkutp.model.Acceso;
import com.utp.parkutp.repository.*;
import com.utp.parkutp.exception.NegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
@Service @Transactional(readOnly=true)
public class AccesoService {
 private final AccesoRepository accesos;private final EstacionamientoRepository estacionamientos;
 public AccesoService(AccesoRepository a,EstacionamientoRepository e){accesos=a;estacionamientos=e;}
 private Acceso modelo(AccesoEntity a){return new Acceso(a.getId(),a.getEstacionamiento().getId(),a.getNombre(),a.getTipo());}
 private AccesoEntity buscarEntidad(long estacionamiento,long id){
  return accesos.findById(id).filter(a->a.getEstacionamiento().getId()==estacionamiento)
   .orElseThrow(()->NegocioException.noEncontrado("El acceso no existe en este estacionamiento"));
 }
 public List<Acceso> listar(long id){
  if(!estacionamientos.existsById(id))throw NegocioException.noEncontrado("El estacionamiento no existe");
  return accesos.findByEstacionamientoIdOrderById(id).stream().map(this::modelo).toList();
 }
 public Acceso buscar(long e,long id){return modelo(buscarEntidad(e,id));}
 @Transactional public Acceso crear(long id,AccesoDto dto){
  var e=estacionamientos.bloquear(id).orElseThrow(()->NegocioException.noEncontrado("El estacionamiento no existe"));
  if(accesos.findByEstacionamientoIdAndTipo(id,dto.getTipo()).isPresent())
   throw NegocioException.conflicto("Ya existe un acceso de ese tipo");
  return modelo(accesos.saveAndFlush(new AccesoEntity(e,dto.getNombre().strip(),dto.getTipo())));
 }
 @Transactional public Acceso actualizar(long e,long id,AccesoDto dto){
  estacionamientos.bloquear(e).orElseThrow(()->NegocioException.noEncontrado("El estacionamiento no existe"));
  var a=buscarEntidad(e,id);
  if(!a.getTipo().equals(dto.getTipo()))throw NegocioException.conflicto("El tipo del acceso no se cambia; conserva el sentido de entrada o salida");
  a.setNombre(dto.getNombre().strip());accesos.flush();return modelo(a);
 }
 @Transactional public void eliminar(long e,long id){
  estacionamientos.bloquear(e).orElseThrow(()->NegocioException.noEncontrado("El estacionamiento no existe"));
  var a=buscarEntidad(e,id);
  try{accesos.delete(a);accesos.flush();}
  catch(DataIntegrityViolationException ex){throw NegocioException.conflicto("El acceso tiene movimientos o dispositivos asociados; conserva su historial");}
 }
}
