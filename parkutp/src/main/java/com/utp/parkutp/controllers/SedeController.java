package com.utp.parkutp.controllers;

import com.utp.parkutp.models.Sede;
import com.utp.parkutp.services.SedeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sedes")
public class SedeController {

    private final SedeService sedeService;

    public SedeController(SedeService sedeService) {
        this.sedeService = sedeService;
    }

    // Status 200 OK
    @GetMapping
    public ResponseEntity<List<Sede>> listarSedes() {
        List<Sede> sedes = sedeService.obtenerTodas();
        return ResponseEntity.ok(sedes);
    }

    // Status 200 OK o 404 Not Found
    @GetMapping("/{id}")
    public ResponseEntity<Sede> obtenerSedePorId(@PathVariable String id) {
        Optional<Sede> sede = sedeService.buscarPorId(id);
        if (sede.isPresent()) {
            return ResponseEntity.ok(sede.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Retorna 404
        }
    }

    // Status 201 Created
    @PostMapping
    public ResponseEntity<Sede> crearSede(@RequestBody Sede sede) {
        Sede nuevaSede = sedeService.guardarSede(sede);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaSede);
    }
}