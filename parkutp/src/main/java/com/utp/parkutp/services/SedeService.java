package com.utp.parkutp.services;

import com.utp.parkutp.models.Sede;
import com.utp.parkutp.repositories.SedeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SedeService {

    private final SedeRepository sedeRepository;

    public SedeService(SedeRepository sedeRepository) {
        this.sedeRepository = sedeRepository;
    }

    public List<Sede> obtenerTodas() {
        return sedeRepository.obtenerTodas();
    }

    public Optional<Sede> obtenerPorId(String id) {
        return sedeRepository.obtenerPorId(id);
    }

    public Sede guardar(Sede sede) {
        return sedeRepository.agregarSede(sede);
    }

    public Sede actualizar(String id, Sede sede) {
        return sedeRepository.actualizarSede(id, sede);
    }

    public boolean eliminar(String id) {
        return sedeRepository.eliminarSede(id);
    }
}