package com.utp.parkutp;
import com.utp.parkutp.dto.*;
import com.utp.parkutp.exception.NegocioException;
import com.utp.parkutp.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc
// No utilizamos mocks: evita activar instrumentación de Mockito en esta suite JDBC.
@TestExecutionListeners({ServletTestExecutionListener.class, DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
@EnabledIfEnvironmentVariable(named="TEST_DB_URL",matches=".*")
class ParkutpIntegrationTest {
 @Autowired JdbcTemplate jdbc;
 @Autowired SedeService sedes;
 @Autowired EstacionamientoService estacionamientos;
 @Autowired MovimientoService movimientos;
 @Autowired MockMvc mvc;
 @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
  String url=System.getenv("TEST_DB_URL");
  if(url==null || !url.matches("jdbc:postgresql://[^/]+/parkutp_test(?:[?].*)?")) throw new IllegalStateException("Utiliza una base dedicada llamada parkutp_test");
  registry.add("spring.datasource.url",()->url);
  registry.add("spring.datasource.username",()->System.getenv().getOrDefault("TEST_DB_USER","parkutp"));
  registry.add("spring.datasource.password",()->System.getenv().getOrDefault("TEST_DB_PASSWORD",""));
 }
 @BeforeEach void preparar() {
  jdbc.execute("TRUNCATE movimiento,dispositivo,acceso,estacionamiento,sede RESTART IDENTITY CASCADE");
  SedeDto dto=new SedeDto();dto.setId("AQP");dto.setNombre("UTP Arequipa");sedes.crear(dto);
 }
 private EstacionamientoDto estacionamiento(int capacidad) { var dto=new EstacionamientoDto();dto.setSedeId("AQP");dto.setNombre("Principal");dto.setCapacidad(capacidad);return dto; }
 private MovimientoDto movimiento(String tipo,UUID evento) { var dto=new MovimientoDto();dto.setEventoId(evento);dto.setTipo(tipo);dto.setOrigen("SIMULADOR");return dto; }
 @Test void crudPersistenteYDosAccesos() {
  var e=estacionamientos.crear(estacionamiento(20));assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM acceso WHERE estacionamiento_id=?",Integer.class,e.id()));
  var cambio=estacionamiento(25);cambio.setNombre("Principal renovado");estacionamientos.actualizar(e.id(),cambio);
  assertEquals(25,estacionamientos.buscar(e.id()).capacidad());
  assertEquals("Principal renovado",jdbc.queryForObject("SELECT nombre FROM estacionamiento WHERE id=?",String.class,e.id()));
  estacionamientos.eliminar(e.id());assertThrows(NegocioException.class,()->estacionamientos.buscar(e.id()));
 }
 @Test void nombreDuplicadoIgnoraMayusculas() {
  estacionamientos.crear(estacionamiento(20));var dto=estacionamiento(20);dto.setNombre(" principal ");
  assertThrows(NegocioException.class,()->estacionamientos.crear(dto));assertEquals(1,estacionamientos.listar().size());
 }
 @Test void ingresoSalidaCambianAforo() {
  long id=estacionamientos.crear(estacionamiento(20)).id();movimientos.registrar(id,movimiento("ENTRADA",UUID.randomUUID()));
  assertEquals(19,estacionamientos.buscar(id).getDisponibles());movimientos.registrar(id,movimiento("SALIDA",UUID.randomUUID()));assertEquals(20,estacionamientos.buscar(id).getDisponibles());
 }
 @Test void eventoReenviadoSeCuentaUnaVez() {
  long id=estacionamientos.crear(estacionamiento(1)).id();var dto=movimiento("ENTRADA",UUID.randomUUID());
  long movimiento=movimientos.registrar(id,dto).id();assertEquals(movimiento,movimientos.registrar(id,dto).id());assertEquals(1,estacionamientos.buscar(id).ocupados());
 }
 @Test void eventoReutilizadoConOtrosDatosEsConflicto() {
  long id=estacionamientos.crear(estacionamiento(2)).id();UUID evento=UUID.randomUUID();movimientos.registrar(id,movimiento("ENTRADA",evento));
  assertThrows(NegocioException.class,()->movimientos.registrar(id,movimiento("SALIDA",evento)));assertEquals(1,estacionamientos.buscar(id).ocupados());
 }
 @Test void noPermiteSalidaConAforoVacio() {
  long id=estacionamientos.crear(estacionamiento(2)).id();assertThrows(NegocioException.class,()->movimientos.registrar(id,movimiento("SALIDA",UUID.randomUUID())));assertEquals(0,movimientos.recientes(id).size());
 }
 @Test void noPermiteIngresoCuandoEstaLleno() {
  long id=estacionamientos.crear(estacionamiento(1)).id();movimientos.registrar(id,movimiento("ENTRADA",UUID.randomUUID()));assertThrows(NegocioException.class,()->movimientos.registrar(id,movimiento("ENTRADA",UUID.randomUUID())));
 }
 @Test void dosIngresosSimultaneosSoloUnoOcupaUltimoEspacio() throws Exception {
  long id=estacionamientos.crear(estacionamiento(1)).id();var ready=new CountDownLatch(2);var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
  Callable<Boolean> ingreso=()->{ready.countDown();start.await(5,TimeUnit.SECONDS);try{movimientos.registrar(id,movimiento("ENTRADA",UUID.randomUUID()));return true;}catch(NegocioException ex){return false;}};
  try { var a=pool.submit(ingreso);var b=pool.submit(ingreso);assertTrue(ready.await(5,TimeUnit.SECONDS));start.countDown();assertNotEquals(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));assertEquals(1,estacionamientos.buscar(id).ocupados()); }
  finally { pool.shutdownNow(); }
 }
 @Test void reduccionCapacidadYBorradoProtegenHistorial() {
  long id=estacionamientos.crear(estacionamiento(3)).id();movimientos.registrar(id,movimiento("ENTRADA",UUID.randomUUID()));movimientos.registrar(id,movimiento("ENTRADA",UUID.randomUUID()));
  assertThrows(NegocioException.class,()->estacionamientos.actualizar(id,estacionamiento(1)));assertThrows(NegocioException.class,()->estacionamientos.eliminar(id));assertEquals(3,estacionamientos.buscar(id).capacidad());
 }
 @Test void inactivoImpideEntradasPeroPermiteSalidas() {
  long id=estacionamientos.crear(estacionamiento(2)).id();movimientos.registrar(id,movimiento("ENTRADA",UUID.randomUUID()));var dto=estacionamiento(2);dto.setActivo(false);estacionamientos.actualizar(id,dto);
  assertThrows(NegocioException.class,()->movimientos.registrar(id,movimiento("ENTRADA",UUID.randomUUID())));movimientos.registrar(id,movimiento("SALIDA",UUID.randomUUID()));assertEquals(0,estacionamientos.buscar(id).ocupados());
 }
 @Test void sedeConEstacionamientosNoSeElimina() { estacionamientos.crear(estacionamiento(2));assertThrows(NegocioException.class,()->sedes.eliminar("AQP"));assertNotNull(sedes.buscar("AQP")); }
 @Test void apiResponde400PorDatosInvalidos() throws Exception {
  mvc.perform(post("/api/estacionamientos").contentType("application/json").content("{\"sedeId\":\"AQP\",\"nombre\":\" \",\"capacidad\":0}")) .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores").isArray());
 }
 @Test void apiCrudY404() throws Exception {
  mvc.perform(get("/api/sedes")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("AQP"));
  mvc.perform(get("/api/estacionamientos/99999")).andExpect(status().isNotFound());
  mvc.perform(post("/api/estacionamientos").contentType("application/json").content("{\"sedeId\":\"AQP\",\"nombre\":\"Principal\",\"capacidad\":20}")) .andExpect(status().isCreated()).andExpect(header().string("Location","/api/estacionamientos/1")).andExpect(jsonPath("$.disponibles").value(20));
  mvc.perform(delete("/api/estacionamientos/1")).andExpect(status().isNoContent());
 }
 @Test void formularioMvcMuestraErroresYNoInserta() throws Exception {
  mvc.perform(post("/estacionamientos").param("sedeId","AQP").param("nombre","").param("capacidad","0")).andExpect(status().isOk()).andExpect(view().name("estacionamientos/formulario")).andExpect(model().attributeHasFieldErrors("form","nombre","capacidad"));
  assertTrue(estacionamientos.listar().isEmpty());
 }
}
