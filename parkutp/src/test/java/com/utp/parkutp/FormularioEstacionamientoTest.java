package com.utp.parkutp;

import com.utp.parkutp.controller.web.ParkingWebController;
import com.utp.parkutp.dto.EstacionamientoDto;
import com.utp.parkutp.exception.NegocioException;
import com.utp.parkutp.service.*;
import org.junit.jupiter.api.*;
import com.utp.parkutp.model.Estacionamiento;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FormularioEstacionamientoTest {
    private MockMvc mvc;
    private ServicioDePrueba estacionamientos;

    private static class ServicioDePrueba extends EstacionamientoService {
        EstacionamientoDto ultimo;
        NegocioException error;
        ServicioDePrueba() { super(null, null); }
        @Override public Estacionamiento crear(EstacionamientoDto dto) { ultimo = dto; return null; }
        @Override public Estacionamiento actualizar(long id, EstacionamientoDto dto) {
            ultimo = dto; if (error != null) throw error; return null;
        }
    }

    @BeforeEach void preparar() {
        estacionamientos = new ServicioDePrueba();
        var sedes = new SedeService(null) {
            @Override public java.util.List<com.utp.parkutp.model.Sede> listar() { return List.of(); }
        };
        mvc = MockMvcBuilders.standaloneSetup(new ParkingWebController(
                estacionamientos, sedes, null)).build();
    }

    @Test void conservaDatosYExplicaCapacidadNoNumerica() throws Exception {
        var resultado = mvc.perform(post("/estacionamientos")
                .param("sedeId", "AQP").param("nombre", "Zona <principal>")
                .param("capacidad", "abc").param("_activo", "on"))
                .andExpect(status().isOk()).andExpect(view().name("estacionamientos/formulario"))
                .andReturn().getModelAndView().getModel();
        var valores = (Map<?, ?>) resultado.get("valores");
        assertEquals("abc", valores.get("capacidad"));
        assertEquals("Zona <principal>", valores.get("nombre"));
        assertEquals("AQP", valores.get("sedeId"));
        assertTrue(((List<?>) resultado.get("errores")).contains("La capacidad debe ser un número entero"));
        assertFalse(((EstacionamientoDto) resultado.get("form")).isActivo());
        assertNull(estacionamientos.ultimo);
    }

    @Test void casillaDesmarcadaDesactivaAlEditar() throws Exception {
        mvc.perform(post("/estacionamientos/1").param("sedeId", "AQP")
                .param("nombre", "Zona").param("capacidad", "10").param("_activo", "on"))
                .andExpect(status().is3xxRedirection());
        assertFalse(estacionamientos.ultimo.isActivo());
    }

    @Test void casillaMarcadaActivaAlEditar() throws Exception {
        mvc.perform(post("/estacionamientos/1").param("sedeId", "AQP")
                .param("nombre", "Zona").param("capacidad", "10")
                .param("_activo", "on").param("activo", "true"))
                .andExpect(status().is3xxRedirection());
        assertTrue(estacionamientos.ultimo.isActivo());
    }

    @Test void muestraErrorDeNegocioYConservaDatos() throws Exception {
        estacionamientos.error = NegocioException.conflicto("La capacidad no puede ser menor que la ocupación");
        mvc.perform(post("/estacionamientos/1").param("sedeId", "AQP")
                .param("nombre", "Zona").param("capacidad", "1").param("activo", "true"))
                .andExpect(view().name("estacionamientos/formulario"))
                .andExpect(model().attribute("errores", List.of("La capacidad no puede ser menor que la ocupación")));
    }
}
