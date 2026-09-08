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

    // Inyección de dependencias mediante constructor
    private final SedeService sedeService;

    public SedeController(SedeService sedeService) {
        this.sedeService = sedeService;
    }

    // READ: lista todas las sedes registradas
    // GET /api/sedes -> 200 OK
    @GetMapping
    public ResponseEntity<List<Sede>> listarSedes() {
        return ResponseEntity.ok(sedeService.obtenerTodas());
    }

    // READ: busca una sede por su ID
    // GET /api/sedes/{id} -> 200 OK / 404 Not Found
    @GetMapping("/{id}")
    public ResponseEntity<Sede> obtenerPorId(@PathVariable String id) {
        Optional<Sede> sede = sedeService.obtenerPorId(id);

        return sede
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE: registra una nueva sede
    // POST /api/sedes -> 201 Created
    @PostMapping
    public ResponseEntity<Sede> crearSede(@RequestBody Sede sede) {
        Sede creada = sedeService.guardar(sede);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creada);
    }

    // UPDATE: actualiza los datos de una sede existente
    // PUT /api/sedes/{id} -> 200 OK / 404 Not Found
    @PutMapping("/{id}")
    public ResponseEntity<Sede> actualizarSede(
            @PathVariable String id,
            @RequestBody Sede sede) {

        Sede actualizada = sedeService.actualizar(id, sede);

        if (actualizada == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(actualizada);
    }

    // DELETE: elimina una sede por su ID
    // DELETE /api/sedes/{id} -> 204 No Content / 404 Not Found
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSede(@PathVariable String id) {

        boolean eliminada = sedeService.eliminar(id);

        if (!eliminada) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}