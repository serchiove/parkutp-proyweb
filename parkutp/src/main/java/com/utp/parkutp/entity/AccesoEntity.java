package com.utp.parkutp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "acceso")
public class AccesoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estacionamiento_id", nullable = false)
    private Long estacionamientoId;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    protected AccesoEntity() {
    }

    public AccesoEntity(Long estacionamientoId, String nombre, String tipo) {
        this.estacionamientoId = estacionamientoId;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public Long getEstacionamientoId() { return estacionamientoId; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
}