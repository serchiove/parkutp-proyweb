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

    public Sede guardarSede(Sede sede) {
        sedeRepository.guardar(sede);
        return sede;
    }

    public Optional<Sede> buscarPorId(String id) {
        return sedeRepository.obtenerTodas().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }
}