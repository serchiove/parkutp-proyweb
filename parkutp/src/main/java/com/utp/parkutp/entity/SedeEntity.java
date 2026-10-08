package com.utp.parkutp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sede")
public class SedeEntity {
    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    protected SedeEntity() {
    }

    public SedeEntity(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}