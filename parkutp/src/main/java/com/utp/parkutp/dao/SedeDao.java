package com.utp.parkutp.dao;
import com.utp.parkutp.model.Sede;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public class SedeDao {
 private final JdbcTemplate jdbc;
 public SedeDao(JdbcTemplate jdbc) { this.jdbc=jdbc; }
 public List<Sede> listar() { return jdbc.query("SELECT id,nombre FROM sede ORDER BY nombre", (rs,n)->new Sede(rs.getString("id"),rs.getString("nombre"))); }
 public Optional<Sede> buscar(String id) { return jdbc.query("SELECT id,nombre FROM sede WHERE id=?", (rs,n)->new Sede(rs.getString("id"),rs.getString("nombre")), id).stream().findFirst(); }
 public void insertar(String id, String nombre) { jdbc.update("INSERT INTO sede(id,nombre) VALUES (?,?)", id,nombre); }
 public int actualizar(String id, String nombre) { return jdbc.update("UPDATE sede SET nombre=? WHERE id=?", nombre,id); }
 public int eliminar(String id) { return jdbc.update("DELETE FROM sede WHERE id=?", id); }
}
