package com.utp.parkutp.model;
public record Estacionamiento(long id, String sedeId, String sedeNombre, String nombre, int capacidad, boolean activo, int ocupados) {
 public long getId() { return id; }
 public String getSedeId() { return sedeId; }
 public String getSedeNombre() { return sedeNombre; }
 public String getNombre() { return nombre; }
 public int getCapacidad() { return capacidad; }
 public boolean isActivo() { return activo; }
 public int getOcupados() { return ocupados; }
 public int getDisponibles() { return capacidad - ocupados; }
 public int getPorcentaje() { return capacidad == 0 ? 0 : ocupados * 100 / capacidad; }
}
