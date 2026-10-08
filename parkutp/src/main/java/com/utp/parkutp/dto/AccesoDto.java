package com.utp.parkutp.dto;
import jakarta.validation.constraints.*;
public class AccesoDto {
 @NotBlank @Size(max=80) private String nombre;
 @NotBlank @Pattern(regexp="ENTRADA|SALIDA") private String tipo;
 public String getNombre(){return nombre;} public void setNombre(String n){nombre=n;}
 public String getTipo(){return tipo;} public void setTipo(String t){tipo=t;}
}
