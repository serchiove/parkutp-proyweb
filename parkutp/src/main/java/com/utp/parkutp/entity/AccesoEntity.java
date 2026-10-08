package com.utp.parkutp.entity;
import jakarta.persistence.*;
@Entity @Table(name="acceso",uniqueConstraints=@UniqueConstraint(columnNames={"estacionamiento_id","tipo"}))
public class AccesoEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="estacionamiento_id",nullable=false) private EstacionamientoEntity estacionamiento;
 @Column(nullable=false,length=80) private String nombre;
 @Column(nullable=false,length=10) private String tipo;
 protected AccesoEntity(){}
 public AccesoEntity(EstacionamientoEntity e,String nombre,String tipo){this.estacionamiento=e;this.nombre=nombre;this.tipo=tipo;}
 public Long getId(){return id;} public EstacionamientoEntity getEstacionamiento(){return estacionamiento;}
 public String getNombre(){return nombre;} public String getTipo(){return tipo;} public void setNombre(String nombre){this.nombre=nombre;}
}
