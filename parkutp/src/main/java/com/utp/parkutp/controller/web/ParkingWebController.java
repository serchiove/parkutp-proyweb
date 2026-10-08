package com.utp.parkutp.controller.web;

import com.utp.parkutp.dto.EstacionamientoDto;
import com.utp.parkutp.exception.NegocioException;
import com.utp.parkutp.model.Estacionamiento;
import com.utp.parkutp.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ParkingWebController {
    private final EstacionamientoService service;
    private final SedeService sedes;
    private final MovimientoService movimientos;

    public ParkingWebController(EstacionamientoService service, SedeService sedes, MovimientoService movimientos) {
        this.service = service;
        this.sedes = sedes;
        this.movimientos = movimientos;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/estacionamientos";
    }

    @GetMapping("/estacionamientos")
    public String listar(Model model) {
        var lista = service.listar();
        int capacidad = 0, ocupados = 0, disponibles = 0;
        for (Estacionamiento e : lista) {
            if (!e.activo())
                continue;
            capacidad += e.capacidad();
            ocupados += e.ocupados();
            disponibles += e.getDisponibles();
        }
        model.addAttribute("estacionamientos", lista);
        model.addAttribute("totalCapacidad", capacidad);
        model.addAttribute("totalOcupados", ocupados);
        model.addAttribute("totalDisponibles", disponibles);
        return "estacionamientos/lista";
    }

    @GetMapping("/estacionamientos/nuevo")
    public String nuevo(Model model) {
        var dto = new EstacionamientoDto();
        model.addAttribute("form", dto);
        return formulario(model, null, dto);
    }

    @GetMapping("/estacionamientos/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        var e = service.buscar(id);
        var dto = new EstacionamientoDto();
        dto.setSedeId(e.sedeId());
        dto.setNombre(e.nombre());
        dto.setCapacidad(e.capacidad());
        dto.setActivo(e.activo());
        model.addAttribute("form", dto);
        return formulario(model, id, dto);
    }

    private String formulario(Model model, Long id, EstacionamientoDto dto) {
        model.addAttribute("sedes", sedes.listar());
        model.addAttribute("id", id);
        var result = (BindingResult) model.getAttribute(BindingResult.MODEL_KEY_PREFIX + "form");
        var valores = new java.util.HashMap<String, Object>();
        valores.put("sedeId", result == null ? dto.getSedeId() : result.getFieldValue("sedeId"));
        valores.put("nombre", result == null ? dto.getNombre() : result.getFieldValue("nombre"));
        valores.put("capacidad", result == null ? dto.getCapacidad() : result.getFieldValue("capacidad"));
        model.addAttribute("valores", valores);
        model.addAttribute("errores", result == null ? java.util.List.of()
                : result.getAllErrors().stream().map(error -> error instanceof org.springframework.validation.FieldError campo
                        && campo.isBindingFailure() ? "La capacidad debe ser un número entero" : error.getDefaultMessage()).toList());
        return "estacionamientos/formulario";
    }

    @PostMapping("/estacionamientos")
    public String crear(@Valid @ModelAttribute("form") EstacionamientoDto dto, BindingResult result, Model model,
            RedirectAttributes flash) {
        if (result.hasErrors())
            return formulario(model, null, dto);
        try {
            service.crear(dto);
        } catch (NegocioException ex) {
            String mensaje = ex.getMessage();
            result.reject("negocio", mensaje != null ? mensaje : "No se pudo completar la operación");
            return formulario(model, null, dto);
        }
        flash.addFlashAttribute("mensaje", "Estacionamiento registrado con sus dos accesos");
        return "redirect:/estacionamientos";
    }

    @PostMapping("/estacionamientos/{id}")
    public String actualizar(@PathVariable long id, @Valid @ModelAttribute("form") EstacionamientoDto dto,
            BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors())
            return formulario(model, id, dto);
        try {
            service.actualizar(id, dto);
        } catch (NegocioException ex) {
            String mensaje = ex.getMessage();
            result.reject("negocio", mensaje != null ? mensaje : "No se pudo completar la operación");
            return formulario(model, id, dto);
        }
        flash.addFlashAttribute("mensaje", "Estacionamiento actualizado");
        return "redirect:/estacionamientos";
    }

    @GetMapping("/estacionamientos/{id}/eliminar")
    public String confirmar(@PathVariable long id, Model model) {
        model.addAttribute("estacionamiento", service.buscar(id));
        return "estacionamientos/eliminar";
    }

    @PostMapping("/estacionamientos/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes flash) {
        try {
            service.eliminar(id);
            flash.addFlashAttribute("mensaje", "Estacionamiento eliminado");
        } catch (NegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/estacionamientos";
    }

    @GetMapping("/estacionamientos/{id}/operacion")
    public String operacion(@PathVariable long id, Model model) {
        model.addAttribute("estacionamiento", service.buscar(id));
        model.addAttribute("movimientos", movimientos.recientes(id));
        return "estacionamientos/operacion";
    }

    @ExceptionHandler(NegocioException.class)
    public String error(NegocioException ex, Model model, jakarta.servlet.http.HttpServletResponse response) {
        response.setStatus(ex.getStatus().value());
        model.addAttribute("error", ex.getMessage());
        return "error-negocio";
    }
}