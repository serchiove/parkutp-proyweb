package com.utp.parkutp.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "movimiento")
public class MovimientoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false, unique = true)
    private UUID eventoId;

    @Column(name = "estacionamiento_id", nullable = false)
    private Long estacionamientoId;

    @Column(name = "acceso_id", nullable = false)
    private Long accesoId;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    @Column(name = "origen", nullable = false, length = 15)
    private String origen;

    @Column(name = "placa", length = 12)
    private String placa;

    @Column(name = "registrado_en", nullable = false)
    private OffsetDateTime registradoEn;

    protected MovimientoEntity() {
    }

    public MovimientoEntity(UUID eventoId, Long estacionamientoId, Long accesoId, String tipo, String origen,
            String placa) {
        this.eventoId = eventoId;
        this.estacionamientoId = estacionamientoId;
        this.accesoId = accesoId;
        this.tipo = tipo;
        this.origen = origen;
        this.placa = placa;
    }

    @PrePersist
    void antesDeGuardar() {
        if (registradoEn == null)
            registradoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public UUID getEventoId() { return eventoId; }
    public Long getEstacionamientoId() { return estacionamientoId; }
    public Long getAccesoId() { return accesoId; }
    public String getTipo() { return tipo; }
    public String getOrigen() { return origen; }
    public String getPlaca() { return placa; }
    public OffsetDateTime getRegistradoEn() { return registradoEn; }
}