package com.utp.parkutp.model;
public record Sede(String id, String nombre) {
 public String getId() { return id; }
 public String getNombre() { return nombre; }
}
