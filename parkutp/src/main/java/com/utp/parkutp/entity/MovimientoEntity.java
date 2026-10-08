package com.utp.parkutp.entity;

import com.utp.parkutp.model.Movimiento;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Mapeo de la tabla existente. La API conserva su modelo de respuesta. */
@Entity(name = "MovimientoPersistido")
@Table(name = "movimiento")
public class MovimientoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "evento_id", nullable = false, unique = true)
    private UUID eventoId;
    @Column(name = "estacionamiento_id", nullable = false)
    private long estacionamientoId;
    @Column(name = "acceso_id", nullable = false)
    private long accesoId;
    @Column(nullable = false, length = 10)
    private String tipo;
    @Column(nullable = false, length = 15)
    private String origen;
    @Column(length = 12)
    private String placa;
    @Column(name = "registrado_en", nullable = false)
    private OffsetDateTime registradoEn;

    protected MovimientoEntity() {
    }

    public Movimiento toModel() {
        return new Movimiento(id, eventoId, estacionamientoId, accesoId, tipo, origen, placa, registradoEn);
    }
}
