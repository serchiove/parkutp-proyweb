package com.utp.parkutp.repositories;

import org.springframework.stereotype.Repository;
import com.utp.parkutp.models.Sede;

import java.util.ArrayList;
import java.util.List;

@Repository
public class SedeRepository {

    // LISTA EN MEMORIA PARA SIMULAR UNA BASE DE DATOS
    private List<Sede> sedes = new ArrayList<>();

    public List<Sede> obtenerTodas() {
        return sedes;
    }

    public Sede agregarSede(Sede sede) {
        sedes.add(sede);
        return sede;
    }
}
