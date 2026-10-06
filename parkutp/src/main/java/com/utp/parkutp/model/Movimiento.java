package com.utp.parkutp.model;
import java.time.OffsetDateTime;
import java.util.UUID;
public record Movimiento(long id, UUID eventoId, long estacionamientoId, long accesoId, String tipo, String origen, String placa, OffsetDateTime registradoEn) {
 public long getId() { return id; }
 public String getTipo() { return tipo; }
 public String getOrigen() { return origen; }
 public String getPlaca() { return placa; }
 public String getFechaTexto() { return registradoEn.atZoneSameInstant(java.time.ZoneId.of("America/Lima")).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")); }
}
