package com.utp.parkutp.controllers;

import com.utp.parkutp.models.Sede;
import com.utp.parkutp.services.SedeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/sedes")
public class SedeController {

    private final SedeService sedeService;

    // Inyección de dependencias (Controller -> Service)
    public SedeController(SedeService sedeService) {
        this.sedeService = sedeService;
    }

    @GetMapping
    public ResponseEntity<List<Sede>> listarSedes() {
        List<Sede> sedes = sedeService.obtenerTodas();
        return ResponseEntity.ok(sedes); // Retorna Status 200 OK
    }
}