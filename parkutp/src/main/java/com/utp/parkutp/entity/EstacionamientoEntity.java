package com.utp.parkutp.entity;
import jakarta.persistence.*;
@Entity @Table(name="estacionamiento")
public class EstacionamientoEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="sede_id",nullable=false) private SedeEntity sede;
 @Column(nullable=false,length=120) private String nombre;
 @Column(nullable=false) private int capacidad;
 @Column(nullable=false) private boolean activo;
 protected EstacionamientoEntity(){}
 public EstacionamientoEntity(SedeEntity sede,String nombre,int capacidad,boolean activo){actualizar(sede,nombre,capacidad,activo);}
 public void actualizar(SedeEntity sede,String nombre,int capacidad,boolean activo){this.sede=sede;this.nombre=nombre;this.capacidad=capacidad;this.activo=activo;}
 public Long getId(){return id;} public SedeEntity getSede(){return sede;}
 public String getNombre(){return nombre;} public int getCapacidad(){return capacidad;} public boolean isActivo(){return activo;}
}
