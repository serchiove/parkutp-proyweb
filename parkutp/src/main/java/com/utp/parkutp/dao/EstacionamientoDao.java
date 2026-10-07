package com.utp.parkutp.dao;

import com.utp.parkutp.model.Estacionamiento;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class EstacionamientoDao {
    private final JdbcTemplate jdbc;
    private static final String SELECT = """
            SELECT e.*, s.nombre AS sede_nombre,
             COALESCE((SELECT SUM(CASE WHEN m.tipo='ENTRADA' THEN 1 ELSE -1 END) FROM movimiento m WHERE m.estacionamiento_id=e.id),0)::int AS ocupados
            FROM estacionamiento e JOIN sede s ON s.id=e.sede_id
            """;
    private final RowMapper<Estacionamiento> mapper = (rs, n) -> new Estacionamiento(rs.getLong("id"),
            rs.getString("sede_id"), rs.getString("sede_nombre"), rs.getString("nombre"), rs.getInt("capacidad"),
            rs.getBoolean("activo"), rs.getInt("ocupados"));

    public EstacionamientoDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Estacionamiento> listar() {
        return jdbc.query(SELECT + " ORDER BY e.id", mapper);
    }

    public Optional<Estacionamiento> buscar(long id) {
        return jdbc.query(SELECT + " WHERE e.id=?", mapper, id).stream().findFirst();
    }

    // Todos los cambios de capacidad y movimientos toman el mismo bloqueo.
    public boolean bloquear(long id) {
        return !jdbc.queryForList("SELECT id FROM estacionamiento WHERE id=? FOR UPDATE", id).isEmpty();
    }

    public long insertar(String sede, String nombre, int capacidad, boolean activo) {
        return jdbc.queryForObject(
                "INSERT INTO estacionamiento(sede_id,nombre,capacidad,activo) VALUES (?,?,?,?) RETURNING id",
                Long.class, sede, nombre, capacidad, activo);
    }

    public void crearAccesos(long id) {
        jdbc.update(
                "INSERT INTO acceso(estacionamiento_id,nombre,tipo) VALUES (?,'Acceso de ingreso','ENTRADA'),(?,'Acceso de salida','SALIDA')",
                id, id);
    }

    public long acceso(long id, String tipo) {
        return jdbc.queryForObject("SELECT id FROM acceso WHERE estacionamiento_id=? AND tipo=?", Long.class, id, tipo);
    }

    public void actualizar(long id, String sede, String nombre, int capacidad, boolean activo) {
        jdbc.update("UPDATE estacionamiento SET sede_id=?,nombre=?,capacidad=?,activo=? WHERE id=?", sede, nombre,
                capacidad, activo, id);
    }

    public long cantidadMovimientos(long id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM movimiento WHERE estacionamiento_id=?", Long.class, id);
    }

    public void eliminar(long id) {
        jdbc.update("DELETE FROM estacionamiento WHERE id=?", id);
    }
}
