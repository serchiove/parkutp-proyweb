package com.utp.parkutp.repositories;

import com.utp.parkutp.models.Sede;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SedeRepository {
    private final List<Sede> sedes = new ArrayList<>();

    public List<Sede> obtenerTodas() {
        return sedes;
    }

    public void guardar(Sede sede) {
        sedes.add(sede);
    }
}