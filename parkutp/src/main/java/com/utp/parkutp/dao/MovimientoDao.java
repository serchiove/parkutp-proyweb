package com.utp.parkutp.dao;

import com.utp.parkutp.model.Movimiento;
import com.utp.parkutp.entity.MovimientoEntity;
import com.utp.parkutp.repository.MovimientoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Repository
public class MovimientoDao {
    private final MovimientoRepository repository;

    public MovimientoDao(MovimientoRepository repository) {
        this.repository = repository;
    }

    public Optional<Movimiento> buscarEvento(UUID evento) {
        return repository.findByEventoId(evento).map(MovimientoEntity::toModel);
    }

    public Movimiento insertar(UUID evento, long estacionamiento, long acceso, String tipo, String origen,
            String placa) {
        return repository.saveAndFlush(new MovimientoEntity(evento, estacionamiento, acceso, tipo, origen, placa))
                .toModel();
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
