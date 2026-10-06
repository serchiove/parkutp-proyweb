package com.utp.parkutp.dto;
import jakarta.validation.constraints.*;
import java.util.UUID;
public class MovimientoDto {
 @NotNull(message="El identificador del evento es obligatorio") private UUID eventoId;
 @NotBlank @Pattern(regexp="ENTRADA|SALIDA", message="El tipo debe ser ENTRADA o SALIDA") private String tipo;
 @NotBlank @Pattern(regexp="MANUAL|SIMULADOR", message="El origen debe ser MANUAL o SIMULADOR") private String origen;
 @Pattern(regexp="(?i)[A-Z0-9-]{3,12}", message="Placa: entre 3 y 12 letras, números o guiones") private String placa;
 public UUID getEventoId() { return eventoId; } public void setEventoId(UUID value) { eventoId=value; }
 public String getTipo() { return tipo; } public void setTipo(String value) { tipo=value; }
 public String getOrigen() { return origen; } public void setOrigen(String value) { origen=value; }
 public String getPlaca() { return placa; } public void setPlaca(String value) { placa=value; }
}
