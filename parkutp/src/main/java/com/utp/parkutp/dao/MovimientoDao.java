package com.utp.parkutp.dao;

import com.utp.parkutp.entity.MovimientoEntity;
import com.utp.parkutp.model.Movimiento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MovimientoDao {
    @PersistenceContext
    private EntityManager em;

    private Movimiento mapear(MovimientoEntity m) {
        return new Movimiento(m.getId(), m.getEventoId(), m.getEstacionamientoId(), m.getAccesoId(), m.getTipo(),
                m.getOrigen(), m.getPlaca(), m.getRegistradoEn());
    }

    public Optional<Movimiento> buscarEvento(UUID evento) {
        return em.createQuery("SELECT m FROM MovimientoEntity m WHERE m.eventoId = :evento", MovimientoEntity.class)
                .setParameter("evento", evento).getResultList().stream().findFirst().map(this::mapear);
    }

    public Movimiento insertar(UUID evento, long estacionamiento, long acceso, String tipo, String origen,
            String placa) {
        var entidad = new MovimientoEntity(evento, estacionamiento, acceso, tipo, origen, placa);
        em.persist(entidad);
        em.flush();
        return mapear(entidad);
    }

    public List<Movimiento> recientes(long id) {
        return recientes(id, null, null, null);
    }

        // JPQL dinámica: los filtros se aplican en la base antes del límite de 50.
    public List<Movimiento> recientes(long id, LocalDate fecha, String placa, String tipo) {
        String placaFiltro = (placa == null || placa.isBlank()) ? null : placa.trim().toUpperCase(Locale.ROOT);
        boolean filtraTipo = tipo != null && !tipo.isBlank();

        StringBuilder jpql = new StringBuilder("SELECT m FROM MovimientoEntity m WHERE m.estacionamientoId = :id");
        if (fecha != null)
            jpql.append(" AND m.registradoEn >= :desde AND m.registradoEn < :hasta");
        if (placaFiltro != null)
            jpql.append(" AND LOCATE(:placa, UPPER(m.placa)) > 0");
        if (filtraTipo)
            jpql.append(" AND m.tipo = :tipo");
        jpql.append(" ORDER BY m.registradoEn DESC, m.id DESC");

        TypedQuery<MovimientoEntity> consulta = em.createQuery(jpql.toString(), MovimientoEntity.class)
                .setParameter("id", id);
        if (fecha != null) {
            ZoneId zona = ZoneId.of("America/Lima");
            consulta.setParameter("desde", fecha.atStartOfDay(zona).toOffsetDateTime());
            consulta.setParameter("hasta", fecha.plusDays(1).atStartOfDay(zona).toOffsetDateTime());
        }
        if (placaFiltro != null)
            consulta.setParameter("placa", placaFiltro);
        if (filtraTipo)
            consulta.setParameter("tipo", tipo);
        return consulta.setMaxResults(50).getResultList().stream().map(this::mapear).toList();
    }
}