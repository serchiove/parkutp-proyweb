package com.utp.parkutp.controller.api;

import com.utp.parkutp.dto.SedeDto;
import com.utp.parkutp.model.Sede;
import com.utp.parkutp.service.SedeService;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sedes")
public class SedeController {
    private final SedeService service;

    public SedeController(SedeService service) {
        this.service = service;
    }

    @GetMapping
    public List<Sede> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Sede buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Sede> crear(@Valid @RequestBody SedeDto dto) {
        Sede s = service.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
        .header(HttpHeaders.LOCATION, "/api/sedes/" + s.id()).body(s);
    }

    @PutMapping("/{id}")
    public Sede actualizar(@PathVariable String id, @Valid @RequestBody SedeDto dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
