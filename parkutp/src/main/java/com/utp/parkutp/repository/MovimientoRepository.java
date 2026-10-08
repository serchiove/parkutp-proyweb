package com.utp.parkutp.repository;

import com.utp.parkutp.entity.MovimientoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repositorio de lectura durante la migración gradual de JDBC a JPA. */
@Transactional(readOnly = true)
public interface MovimientoRepository extends Repository<MovimientoEntity, Long> {
    @Query("select coalesce(sum(case when m.tipo='ENTRADA' then 1 else -1 end),0) from MovimientoPersistido m where m.estacionamientoId=:id")
    long ocupados(@Param("id") long id);
    @Query("select count(m) from MovimientoPersistido m where m.estacionamientoId=:id")
    long contarPorEstacionamiento(@Param("id") long id);
    Optional<MovimientoEntity> findByEventoId(UUID eventoId);

    default List<MovimientoEntity> buscarHistorial(long estacionamientoId, OffsetDateTime desde,
            OffsetDateTime hasta, String placa, String tipo, Pageable limite) {
        // PostgreSQL debe recibir parámetros tipados incluso cuando no se aplica un
        // filtro.
        OffsetDateTime referencia = OffsetDateTime.parse("2000-01-01T00:00:00Z");
        return consultarHistorial(estacionamientoId, desde != null, desde == null ? referencia : desde,
                hasta != null, hasta == null ? referencia : hasta,
                placa == null ? "" : placa, tipo == null ? "" : tipo, limite);
    }

    @Query("""
            select m from MovimientoPersistido m
            where m.estacionamientoId = :estacionamientoId
              and (:filtrarDesde = false or m.registradoEn >= :desde)
              and (:filtrarHasta = false or m.registradoEn < :hasta)
              and (:placa = '' or locate(upper(:placa), upper(m.placa)) > 0)
              and (:tipo = '' or m.tipo = :tipo)
            order by m.registradoEn desc, m.id desc
            """)
    List<MovimientoEntity> consultarHistorial(
            @Param("estacionamientoId") long estacionamientoId,
            @Param("filtrarDesde") boolean filtrarDesde,
            @Param("desde") OffsetDateTime desde,
            @Param("filtrarHasta") boolean filtrarHasta,
            @Param("hasta") OffsetDateTime hasta,
            @Param("placa") String placa,
            @Param("tipo") String tipo,
            Pageable limite);
}
