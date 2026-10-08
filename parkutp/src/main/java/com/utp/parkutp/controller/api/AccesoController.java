package com.utp.parkutp.controller.api;
import com.utp.parkutp.service.AccesoService;
import com.utp.parkutp.dto.AccesoDto;
import com.utp.parkutp.model.Acceso;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;
@RestController @RequestMapping("/api/estacionamientos/{estacionamientoId}/accesos")
public class AccesoController {
 private final AccesoService service;
 public AccesoController(AccesoService service){this.service=service;}
 @GetMapping public List<Acceso> listar(@PathVariable long estacionamientoId){return service.listar(estacionamientoId);}
 @GetMapping("/{id}") public Acceso buscar(@PathVariable long estacionamientoId,@PathVariable long id){return service.buscar(estacionamientoId,id);}
 @PostMapping public ResponseEntity<Acceso> crear(@PathVariable long estacionamientoId,@Valid @RequestBody AccesoDto dto){
  var a=service.crear(estacionamientoId,dto);
  return ResponseEntity.created(URI.create("/api/estacionamientos/"+estacionamientoId+"/accesos/"+a.id())).body(a);
 }
 @PutMapping("/{id}") public Acceso actualizar(@PathVariable long estacionamientoId,@PathVariable long id,@Valid @RequestBody AccesoDto dto){return service.actualizar(estacionamientoId,id,dto);}
 @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable long estacionamientoId,@PathVariable long id){service.eliminar(estacionamientoId,id);return ResponseEntity.noContent().build();}
}
