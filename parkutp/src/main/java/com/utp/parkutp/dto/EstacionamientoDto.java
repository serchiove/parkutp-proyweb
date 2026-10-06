package com.utp.parkutp.dto;
import jakarta.validation.constraints.*;
public class EstacionamientoDto {
 @NotBlank(message="Selecciona una sede") @Size(max=64) private String sedeId;
 @NotBlank(message="El nombre es obligatorio") @Size(max=120, message="Máximo 120 caracteres") private String nombre;
 @NotNull(message="La capacidad es obligatoria") @Min(value=1, message="La capacidad mínima es 1") @Max(value=10000, message="La capacidad máxima es 10000") private Integer capacidad;
 private boolean activo=true;
 public String getSedeId() { return sedeId; } public void setSedeId(String value) { sedeId=value; }
 public String getNombre() { return nombre; } public void setNombre(String value) { nombre=value; }
 public Integer getCapacidad() { return capacidad; } public void setCapacidad(Integer value) { capacidad=value; }
 public boolean isActivo() { return activo; } public void setActivo(boolean value) { activo=value; }
}
