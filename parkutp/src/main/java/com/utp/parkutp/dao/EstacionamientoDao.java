package com.utp.parkutp.dao;

import com.utp.parkutp.entity.AccesoEntity;
import com.utp.parkutp.entity.EstacionamientoEntity;
import com.utp.parkutp.model.Estacionamiento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class EstacionamientoDao {
    @PersistenceContext
    private EntityManager em;

    // JPQL: aforo = entradas - salidas, calculado con una subconsulta
    private static final String SELECT = """
            SELECT e.id, e.sedeId, s.nombre, e.nombre, e.capacidad, e.activo,
                   (SELECT COALESCE(SUM(CASE WHEN m.tipo = 'ENTRADA' THEN 1 ELSE -1 END), 0L)
                      FROM MovimientoEntity m WHERE m.estacionamientoId = e.id)
              FROM EstacionamientoEntity e JOIN SedeEntity s ON s.id = e.sedeId
            """;

    private Estacionamiento mapear(Object[] r) {
        return new Estacionamiento(((Number) r[0]).longValue(), (String) r[1], (String) r[2], (String) r[3],
                ((Number) r[4]).intValue(), (Boolean) r[5], ((Number) r[6]).intValue());
    }

    public List<Estacionamiento> listar() {
        return em.createQuery(SELECT + " ORDER BY e.id", Object[].class)
                .getResultList().stream().map(this::mapear).toList();
    }

    public Optional<Estacionamiento> buscar(long id) {
        return em.createQuery(SELECT + " WHERE e.id = :id", Object[].class)
                .setParameter("id", id).getResultList().stream().findFirst().map(this::mapear);
    }

    // Todos los cambios de capacidad y movimientos toman el mismo bloqueo (SELECT ... FOR UPDATE).
    public boolean bloquear(long id) {
        return em.find(EstacionamientoEntity.class, id, LockModeType.PESSIMISTIC_WRITE) != null;
    }

    public long insertar(String sede, String nombre, int capacidad, boolean activo) {
        var entidad = new EstacionamientoEntity(sede, nombre, capacidad, activo);
        em.persist(entidad);
        em.flush();
        return entidad.getId();
    }

    public void crearAccesos(long id) {
        em.persist(new AccesoEntity(id, "Acceso de ingreso", "ENTRADA"));
        em.persist(new AccesoEntity(id, "Acceso de salida", "SALIDA"));
        em.flush();
    }

    public long acceso(long id, String tipo) {
        return em.createQuery("SELECT a.id FROM AccesoEntity a WHERE a.estacionamientoId = :id AND a.tipo = :tipo",
                Long.class).setParameter("id", id).setParameter("tipo", tipo).getSingleResult();
    }

    public void actualizar(long id, String sede, String nombre, int capacidad, boolean activo) {
        var entidad = em.find(EstacionamientoEntity.class, id);
        if (entidad == null)
            return;
        entidad.actualizar(sede, nombre, capacidad, activo);
        em.flush();
    }

    public long cantidadMovimientos(long id) {
        return em.createQuery("SELECT COUNT(m) FROM MovimientoEntity m WHERE m.estacionamientoId = :id", Long.class)
                .setParameter("id", id).getSingleResult();
    }

    public void eliminar(long id) {
        em.createQuery("DELETE FROM EstacionamientoEntity e WHERE e.id = :id")
                .setParameter("id", id).executeUpdate();
    }
}