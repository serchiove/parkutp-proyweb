package com.utp.parkutp.dto;
import jakarta.validation.constraints.*;
public class SedeDto {
 @NotBlank(message="El código es obligatorio") @Pattern(regexp="[A-Za-z0-9_-]{1,64}", message="Usa letras, números, guion o guion bajo; máximo 64 caracteres")
 private String id;
 @NotBlank(message="El nombre es obligatorio") @Size(max=120, message="Máximo 120 caracteres") private String nombre;
 public String getId() { return id; } public void setId(String id) { this.id=id; }
 public String getNombre() { return nombre; } public void setNombre(String nombre) { this.nombre=nombre; }
}
