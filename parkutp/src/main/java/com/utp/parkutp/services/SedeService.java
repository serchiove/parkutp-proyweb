package com.utp.parkutp.services;

import com.utp.parkutp.models.Sede;
import java.util.List;
import org.springframework.stereotype.Service;
import com.utp.parkutp.repositories.SedeRepository;

@Service
public class SedeService {

    private final SedeRepository sedeRepository;

    public SedeService(SedeRepository sedeRepository) {
        this.sedeRepository = sedeRepository;
    }

    public List<Sede> obtenerTodas() {
        return sedeRepository.obtenerTodas();
    }
}
