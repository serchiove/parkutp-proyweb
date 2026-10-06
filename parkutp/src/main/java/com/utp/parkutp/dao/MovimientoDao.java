package com.utp.parkutp.dao;
import com.utp.parkutp.model.Movimiento;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.*;
@Repository
public class MovimientoDao {
 private final JdbcTemplate jdbc;
 private final RowMapper<Movimiento> mapper=(rs,n)->new Movimiento(rs.getLong("id"),rs.getObject("evento_id",UUID.class),rs.getLong("estacionamiento_id"),rs.getLong("acceso_id"),rs.getString("tipo"),rs.getString("origen"),rs.getString("placa"),rs.getObject("registrado_en",OffsetDateTime.class));
 public MovimientoDao(JdbcTemplate jdbc) { this.jdbc=jdbc; }
 public Optional<Movimiento> buscarEvento(UUID evento) { return jdbc.query("SELECT * FROM movimiento WHERE evento_id=?",mapper,evento).stream().findFirst(); }
 public Movimiento insertar(UUID evento,long estacionamiento,long acceso,String tipo,String origen,String placa) {
  return jdbc.queryForObject("INSERT INTO movimiento(evento_id,estacionamiento_id,acceso_id,tipo,origen,placa) VALUES (?,?,?,?,?,?) RETURNING *",mapper,evento,estacionamiento,acceso,tipo,origen,placa);
 }
 public List<Movimiento> recientes(long id) { return jdbc.query("SELECT * FROM movimiento WHERE estacionamiento_id=? ORDER BY registrado_en DESC,id DESC LIMIT 50",mapper,id); }
}
