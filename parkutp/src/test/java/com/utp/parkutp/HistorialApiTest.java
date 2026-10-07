package com.utp.parkutp;

import com.utp.parkutp.controller.api.*;
import com.utp.parkutp.dao.*;
import com.utp.parkutp.service.*;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HistorialApiTest {
 private MockMvc mvc;
 private MovimientoDao dao;
 @BeforeEach void preparar() {
  dao=mock(MovimientoDao.class);
  var estacionamientos=mock(EstacionamientoService.class);
  var movimientos=new MovimientoService(estacionamientos,mock(EstacionamientoDao.class),dao);
  mvc=MockMvcBuilders.standaloneSetup(new EstacionamientoController(estacionamientos,movimientos))
   .setControllerAdvice(new ApiExceptionHandler()).build();
 }
 @Test void consultaSinFiltrosConservaCompatibilidad() throws Exception {
  when(dao.recientes(1,null,null,null)).thenReturn(List.of());
  mvc.perform(get("/api/estacionamientos/1/movimientos")).andExpect(status().isOk()).andExpect(content().json("[]"));
  verify(dao).recientes(1,null,null,null);
 }
 @Test void combinaFechaPlacaYTipo() throws Exception {
  mvc.perform(get("/api/estacionamientos/1/movimientos").param("fecha","2026-10-06").param("placa","abc").param("tipo","ENTRADA"))
   .andExpect(status().isOk());
  verify(dao).recientes(1,LocalDate.of(2026,10,6),"abc","ENTRADA");
 }
 @Test void rechazaFechaInvalidaSinConsultarDatos() throws Exception {
  mvc.perform(get("/api/estacionamientos/1/movimientos").param("fecha","2026-02-30"))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensaje").exists());
  verifyNoInteractions(dao);
 }
 @Test void rechazaTipoDesconocido() throws Exception {
  mvc.perform(get("/api/estacionamientos/1/movimientos").param("tipo","OTRO"))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensaje").value("El tipo debe ser ENTRADA o SALIDA"));
  verifyNoInteractions(dao);
 }
 @Test void rechazaPlacaDemasiadoLarga() throws Exception {
  mvc.perform(get("/api/estacionamientos/1/movimientos").param("placa","ABCDEFGHIJKLM"))
   .andExpect(status().isBadRequest());
  verifyNoInteractions(dao);
 }
}
