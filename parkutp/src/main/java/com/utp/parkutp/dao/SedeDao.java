package com.utp.parkutp.dao;

import com.utp.parkutp.entity.SedeEntity;
import com.utp.parkutp.model.Sede;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class SedeDao {
    @PersistenceContext
    private EntityManager em;

    private Sede mapear(SedeEntity e) {
        return new Sede(e.getId(), e.getNombre());
    }

    public List<Sede> listar() {
        return em.createQuery("SELECT s FROM SedeEntity s ORDER BY s.nombre", SedeEntity.class)
                .getResultList().stream().map(this::mapear).toList();
    }

    public Optional<Sede> buscar(String id) {
        return Optional.ofNullable(em.find(SedeEntity.class, id)).map(this::mapear);
    }

    public void insertar(String id, String nombre) {
        em.persist(new SedeEntity(id, nombre));
        em.flush(); // fuerza aquí las violaciones de unicidad para que el Service las traduzca
    }

    public int actualizar(String id, String nombre) {
        return em.createQuery("UPDATE SedeEntity s SET s.nombre = :nombre WHERE s.id = :id")
                .setParameter("nombre", nombre).setParameter("id", id).executeUpdate();
    }

    public int eliminar(String id) {
        return em.createQuery("DELETE FROM SedeEntity s WHERE s.id = :id")
                .setParameter("id", id).executeUpdate();
    }
}