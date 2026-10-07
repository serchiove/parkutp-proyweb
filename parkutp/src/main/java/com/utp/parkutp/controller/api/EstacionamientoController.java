package com.utp.parkutp.controller.api;

import com.utp.parkutp.dto.*;
import com.utp.parkutp.model.*;
import com.utp.parkutp.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/estacionamientos")
public class EstacionamientoController {
    private final EstacionamientoService service;
    private final MovimientoService movimientos;

    public EstacionamientoController(EstacionamientoService service, MovimientoService movimientos) {
        this.service = service;
        this.movimientos = movimientos;
    }

    @GetMapping
    public List<Estacionamiento> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Estacionamiento buscar(@PathVariable long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Estacionamiento> crear(@Valid @RequestBody EstacionamientoDto dto) {
        Estacionamiento e = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/estacionamientos/" + e.id())).body(e);
    }

    @PutMapping("/{id}")
    public Estacionamiento actualizar(@PathVariable long id, @Valid @RequestBody EstacionamientoDto dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/movimientos")
    public List<Movimiento> recientes(@PathVariable long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String placa, @RequestParam(required = false) String tipo) {
        return movimientos.recientes(id, fecha, placa, tipo);
    }

    // 200 también al reenviar el mismo evento: no vuelve a incrementar el aforo.
    @PostMapping("/{id}/movimientos")
    public Movimiento registrar(@PathVariable long id, @Valid @RequestBody MovimientoDto dto) {
        return movimientos.registrar(id, dto);
    }
}
