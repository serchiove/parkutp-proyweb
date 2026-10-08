package com.utp.parkutp.dao;
import com.utp.parkutp.model.Sede;
import com.utp.parkutp.entity.SedeEntity;
import com.utp.parkutp.repository.SedeRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
/** Adaptador JPA que conserva las respuestas de API y vistas. */
@Repository @Transactional(readOnly=true)
public class SedeDao {
 private final SedeRepository repository; private final EntityManager em;
 public SedeDao(SedeRepository repository,EntityManager em){this.repository=repository;this.em=em;}
 public List<Sede> listar(){return repository.findAllByOrderByNombreAsc().stream().map(SedeEntity::toModel).toList();}
 public Optional<Sede> buscar(String id){return repository.findById(id).map(SedeEntity::toModel);}
 @Transactional public void insertar(String id,String nombre){
  // persist no convierte un código repetido en actualización.
  em.persist(new SedeEntity(id,nombre));em.flush();
 }
 @Transactional public int actualizar(String id,String nombre){
  var e=repository.findById(id);if(e.isEmpty())return 0;e.get().setNombre(nombre);repository.flush();return 1;
 }
 @Transactional public int eliminar(String id){
  var e=repository.findById(id);if(e.isEmpty())return 0;repository.delete(e.get());repository.flush();return 1;
 }
}
