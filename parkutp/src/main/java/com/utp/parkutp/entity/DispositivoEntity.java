package com.utp.parkutp.entity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
/** Mapeo de apoyo, sin operación física de sensores. */
@Entity @Table(name="dispositivo")
public class DispositivoEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="acceso_id",nullable=false) private AccesoEntity acceso;
 @Column(nullable=false,unique=true,length=80) private String codigo;
 @Column(nullable=false) private boolean activo;
 @Column(name="ultima_comunicacion") private OffsetDateTime ultimaComunicacion;
 protected DispositivoEntity(){}
}
