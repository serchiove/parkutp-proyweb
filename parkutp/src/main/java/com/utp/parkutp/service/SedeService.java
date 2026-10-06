package com.utp.parkutp.service;
import com.utp.parkutp.dao.SedeDao;
import com.utp.parkutp.dto.SedeDto;
import com.utp.parkutp.model.Sede;
import com.utp.parkutp.exception.NegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class SedeService {
 private final SedeDao dao;
 public SedeService(SedeDao dao) { this.dao=dao; }
 public List<Sede> listar() { return dao.listar(); }
 public Sede buscar(String id) { return dao.buscar(id).orElseThrow(()->NegocioException.noEncontrado("La sede no existe")); }
 @Transactional public Sede crear(SedeDto dto) {
  try { dao.insertar(dto.getId().trim(),dto.getNombre().strip()); }
  catch(DataIntegrityViolationException ex) { throw NegocioException.conflicto("Ya existe una sede con ese código o nombre"); }
  return buscar(dto.getId().trim());
 }
 @Transactional public Sede actualizar(String id,SedeDto dto) {
  try { if(dao.actualizar(id,dto.getNombre().strip())==0) throw NegocioException.noEncontrado("La sede no existe"); }
  catch(DataIntegrityViolationException ex) { throw NegocioException.conflicto("Ya existe otra sede con ese nombre"); }
  return buscar(id);
 }
 @Transactional public void eliminar(String id) {
  try { if(dao.eliminar(id)==0) throw NegocioException.noEncontrado("La sede no existe"); }
  catch(DataIntegrityViolationException ex) { throw NegocioException.conflicto("La sede tiene estacionamientos asociados"); }
 }
}
