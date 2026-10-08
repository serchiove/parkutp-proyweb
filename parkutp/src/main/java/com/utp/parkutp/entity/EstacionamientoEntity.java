package com.utp.parkutp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "estacionamiento")
public class EstacionamientoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sede_id", nullable = false, length = 64)
    private String sedeId;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "capacidad", nullable = false)
    private int capacidad;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    protected EstacionamientoEntity() {
    }

    public EstacionamientoEntity(String sedeId, String nombre, int capacidad, boolean activo) {
        this.sedeId = sedeId;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public String getSedeId() { return sedeId; }
    public String getNombre() { return nombre; }
    public int getCapacidad() { return capacidad; }
    public boolean isActivo() { return activo; }

    public void actualizar(String sedeId, String nombre, int capacidad, boolean activo) {
        this.sedeId = sedeId;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.activo = activo;
    }
}