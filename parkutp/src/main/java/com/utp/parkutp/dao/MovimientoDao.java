package com.utp.parkutp.dao;

import com.utp.parkutp.model.Movimiento;
import com.utp.parkutp.entity.MovimientoEntity;
import com.utp.parkutp.repository.MovimientoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Repository
public class MovimientoDao {
    private final JdbcTemplate jdbc;
    private final MovimientoRepository repository;
    private final RowMapper<Movimiento> mapper = (rs, n) -> new Movimiento(rs.getLong("id"),
            rs.getObject("evento_id", UUID.class), rs.getLong("estacionamiento_id"), rs.getLong("acceso_id"),
            rs.getString("tipo"), rs.getString("origen"), rs.getString("placa"),
            rs.getObject("registrado_en", OffsetDateTime.class));

    public MovimientoDao(JdbcTemplate jdbc, MovimientoRepository repository) {
        this.jdbc = jdbc;
        this.repository = repository;
    }

    public Optional<Movimiento> buscarEvento(UUID evento) {
        return repository.findByEventoId(evento).map(MovimientoEntity::toModel);
    }

    public Movimiento insertar(UUID evento, long estacionamiento, long acceso, String tipo, String origen,
            String placa) {
        return jdbc.queryForObject(
                "INSERT INTO movimiento(evento_id,estacionamiento_id,acceso_id,tipo,origen,placa) VALUES (?,?,?,?,?,?) RETURNING *",
                mapper, evento, estacionamiento, acceso, tipo, origen, placa);
    }

    public List<Movimiento> recientes(long id) {
        return recientes(id, null, null, null);
    }

    public List<Movimiento> recientes(long id, LocalDate fecha, String placa, String tipo) {
        OffsetDateTime desde = null, hasta = null;
        if (fecha != null) {
            ZoneId zona = ZoneId.of("America/Lima");
            desde = fecha.atStartOfDay(zona).toOffsetDateTime();
            hasta = fecha.plusDays(1).atStartOfDay(zona).toOffsetDateTime();
        }
        String placaNormalizada = placa == null || placa.isBlank() ? null : placa.trim().toUpperCase(Locale.ROOT);
        String tipoNormalizado = tipo == null || tipo.isBlank() ? null : tipo;
        return repository.buscarHistorial(id, desde, hasta, placaNormalizada, tipoNormalizado, PageRequest.of(0, 50))
            .stream().map(MovimientoEntity::toModel).toList();
    }
}
