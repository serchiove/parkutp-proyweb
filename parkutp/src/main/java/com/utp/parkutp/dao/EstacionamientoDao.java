package com.utp.parkutp.dao;
import com.utp.parkutp.model.Estacionamiento;
import com.utp.parkutp.entity.*;
import com.utp.parkutp.repository.*;
import com.utp.parkutp.exception.NegocioException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Repository @Transactional(readOnly=true)
public class EstacionamientoDao {
 private final EstacionamientoRepository repository;
 private final SedeRepository sedes;private final AccesoRepository accesos;private final MovimientoRepository movimientos;
 public EstacionamientoDao(EstacionamientoRepository repository,SedeRepository sedes,AccesoRepository accesos,MovimientoRepository movimientos){
  this.repository=repository;this.sedes=sedes;this.accesos=accesos;this.movimientos=movimientos;
 }
 private Estacionamiento toModel(EstacionamientoEntity e){
  return new Estacionamiento(e.getId(),e.getSede().getId(),e.getSede().getNombre(),e.getNombre(),e.getCapacidad(),e.isActivo(),Math.toIntExact(movimientos.ocupados(e.getId())));
 }
 public List<Estacionamiento> listar(){return repository.listarConSede().stream().map(this::toModel).toList();}
 public Optional<Estacionamiento> buscar(long id){return repository.findById(id).map(this::toModel);}
 @Transactional public boolean bloquear(long id){return repository.bloquear(id).isPresent();}
 @Transactional public long insertar(String sede,String nombre,int capacidad,boolean activo){
  return repository.saveAndFlush(new EstacionamientoEntity(sedes.getReferenceById(sede),nombre,capacidad,activo)).getId();
 }
 @Transactional public void crearAccesos(long id){
  var e=repository.getReferenceById(id);
  accesos.saveAllAndFlush(List.of(new AccesoEntity(e,"Acceso de ingreso","ENTRADA"),new AccesoEntity(e,"Acceso de salida","SALIDA")));
 }
 public long acceso(long id,String tipo){
  return accesos.findByEstacionamientoIdAndTipo(id,tipo).orElseThrow(()->NegocioException.conflicto("No está configurado el acceso de "+tipo)).getId();
 }
 @Transactional public void actualizar(long id,String sede,String nombre,int capacidad,boolean activo){
  var e=repository.findById(id).orElseThrow(()->NegocioException.noEncontrado("El estacionamiento no existe"));
  e.actualizar(sedes.getReferenceById(sede),nombre,capacidad,activo);repository.flush();
 }
 public long cantidadMovimientos(long id){return movimientos.contarPorEstacionamiento(id);}
 @Transactional public void eliminar(long id){
  // Las FK de movimientos y dispositivos impiden borrar historial asociado.
  accesos.deleteAll(accesos.findByEstacionamientoIdOrderById(id));accesos.flush();
  repository.deleteById(id);repository.flush();
 }
}
