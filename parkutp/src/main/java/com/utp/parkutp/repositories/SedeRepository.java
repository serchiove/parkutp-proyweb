package com.utp.parkutp.repositories;

import com.utp.parkutp.models.Sede;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SedeRepository {

    private final List<Sede> sedes = new ArrayList<>();

    public List<Sede> obtenerTodas() {
        return sedes;
    }

    public Optional<Sede> obtenerPorId(String id) {
        return sedes.stream()
                .filter(sede -> sede.getId().equals(id))
                .findFirst();
    }

    public Sede agregarSede(Sede sede) {
        sedes.add(sede);
        return sede;
    }

    public Sede actualizarSede(String id, Sede datosNuevos) {
        Optional<Sede> sedeEncontrada = obtenerPorId(id);

        if (sedeEncontrada.isPresent()) {
            Sede sede = sedeEncontrada.get();
            sede.setNombre(datosNuevos.getNombre());
            return sede;
        }

        return null;
    }

    public boolean eliminarSede(String id) {
        return sedes.removeIf(sede -> sede.getId().equals(id));
    }
}