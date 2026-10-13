package com.utp.parkutp;

import com.utp.parkutp.entity.UsuarioEntity;
import com.utp.parkutp.repository.UsuarioRepository;
import com.utp.parkutp.security.JwtService;
import com.utp.parkutp.security.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestExecutionListeners({ ServletTestExecutionListener.class, DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class })
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".*")
@TestPropertySource(properties = { "parkutp.jwt.secret=" + SeguridadApiIntegrationTest.SECRETO,
    "CORS_ORIGENES=http://localhost:4200" })
class SeguridadApiIntegrationTest {
  static final String SECRETO = "secreto-de-pruebas-apf2-con-mas-de-32-caracteres";
  private static final String SECRETO_AJENO = "clave-ajena-de-pruebas-apf2-con-mas-de-32-caracteres";
  private static final String CLAVE = "ClaveDePruebasApf2";
  private static final String JSON = "application/json";

  @Autowired
  MockMvc mvc;
  @Autowired
  JwtService jwt;
  @Autowired
  UsuarioRepository usuarios;
  @Autowired
  PasswordEncoder encoder;

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    String url = System.getenv("TEST_DB_URL");
    if (url == null || !url.matches("jdbc:postgresql://[^/]+/parkutp_test(?:[?].*)?"))
      throw new IllegalStateException("Utiliza una base dedicada llamada parkutp_test");
    registry.add("spring.datasource.url", () -> url);
    registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("TEST_DB_USER", "parkutp"));
    registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("TEST_DB_PASSWORD", ""));
  }

  @BeforeEach
  void usuariosDePrueba() {
    crearUsuario("prueba-admin", Rol.ADMIN);
    crearUsuario("prueba-operador", Rol.OPERADOR);
    crearUsuario("prueba-consulta", Rol.CONSULTA);
  }

  // Siempre se recrean con la clave conocida: no dependen de lo que haya en la base de pruebas.
  private void crearUsuario(String username, Rol rol) {
    usuarios.findByUsernameIgnoreCase(username).ifPresent(usuarios::delete);
    usuarios.save(new UsuarioEntity(username, encoder.encode(CLAVE), rol));
  }

  private String token(Rol rol) {
    return jwt.generar("prueba-" + rol.name().toLowerCase(), rol);
  }

  private static String bearer(String token) {
    return "Bearer " + token;
  }

  // La petición pasó la autorización (el resultado puede ser 400, 404... pero no 401 ni 403).
  private static ResultMatcher autorizado() {
    return resultado -> {
      int estado = resultado.getResponse().getStatus();
      assertNotEquals(401, estado);
      assertNotEquals(403, estado);
    };
  }

  // ---------- Login ----------

  @Test
  void loginCorrectoDevuelveTokenUtilizable() throws Exception {
    String respuesta = mvc.perform(post("/api/auth/login").contentType(JSON)
        .content("{\"username\":\"prueba-consulta\",\"password\":\"" + CLAVE + "\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("Bearer"))
        .andExpect(jsonPath("$.rol").value("CONSULTA")).andExpect(jsonPath("$.token").isNotEmpty())
        .andReturn().getResponse().getContentAsString();
    String token = respuesta.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    mvc.perform(get("/api/auth/yo").header("Authorization", bearer(token))).andExpect(status().isOk())
        .andExpect(jsonPath("$.usuario").value("prueba-consulta")).andExpect(jsonPath("$.rol").value("CONSULTA"));
  }

  @Test
  void loginConClaveIncorrectaDa401() throws Exception {
    mvc.perform(post("/api/auth/login").contentType(JSON)
        .content("{\"username\":\"prueba-admin\",\"password\":\"incorrecta\"}"))
        .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.mensaje").exists());
  }

  @Test
  void loginConUsuarioInexistenteDa401() throws Exception {
    mvc.perform(post("/api/auth/login").contentType(JSON)
        .content("{\"username\":\"no-existe\",\"password\":\"" + CLAVE + "\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void laClaveSeGuardaConBcrypt() {
    String hash = usuarios.findByUsernameIgnoreCase("prueba-admin").orElseThrow().getPasswordHash();
    assertTrue(hash.startsWith("$2"));
    assertNotEquals(CLAVE, hash);
  }

  // ---------- 401: token ausente, alterado, vencido o ajeno ----------

  @Test
  void sinTokenDa401ConEncabezadoBearer() throws Exception {
    mvc.perform(get("/api/sedes")).andExpect(status().isUnauthorized())
        .andExpect(header().string("WWW-Authenticate", "Bearer")).andExpect(jsonPath("$.mensaje").exists());
  }

  @Test
  void tokenAlteradoDa401() throws Exception {
    String[] partes = token(Rol.ADMIN).split("\\.");
    char primero = partes[2].charAt(0);
    partes[2] = (primero == 'A' ? 'B' : 'A') + partes[2].substring(1);
    String alterado = String.join(".", partes);
    mvc.perform(get("/api/sedes").header("Authorization", bearer(alterado))).andExpect(status().isUnauthorized());
  }

  @Test
  void tokenVencidoDa401() throws Exception {
    String vencido = new JwtService(SECRETO, -5).generar("prueba-admin", Rol.ADMIN);
    mvc.perform(get("/api/sedes").header("Authorization", bearer(vencido))).andExpect(status().isUnauthorized());
  }

  @Test
  void tokenFirmadoConOtraClaveDa401() throws Exception {
    String ajeno = new JwtService(SECRETO_AJENO, 60).generar("prueba-admin", Rol.ADMIN);
    mvc.perform(get("/api/sedes").header("Authorization", bearer(ajeno))).andExpect(status().isUnauthorized());
  }

  // ---------- 403 y 2xx según el rol ----------

  @Test
  void consultaLeePeroNoEscribeNiRegistra() throws Exception {
    String t = bearer(token(Rol.CONSULTA));
    mvc.perform(get("/api/sedes").header("Authorization", t)).andExpect(status().isOk());
    mvc.perform(get("/api/estacionamientos").header("Authorization", t)).andExpect(status().isOk());
    mvc.perform(post("/api/sedes").header("Authorization", t).contentType(JSON).content("{}"))
        .andExpect(status().isForbidden());
    mvc.perform(post("/api/estacionamientos").header("Authorization", t).contentType(JSON).content("{}"))
        .andExpect(status().isForbidden());
    mvc.perform(post("/api/estacionamientos/99999/movimientos").header("Authorization", t).contentType(JSON)
        .content("{}")).andExpect(status().isForbidden());
    mvc.perform(delete("/api/estacionamientos/99999").header("Authorization", t))
        .andExpect(status().isForbidden());
  }

  @Test
  void operadorRegistraPeroNoAdministra() throws Exception {
    String t = bearer(token(Rol.OPERADOR));
    mvc.perform(get("/api/sedes").header("Authorization", t)).andExpect(status().isOk());
    mvc.perform(post("/api/estacionamientos/99999/movimientos").header("Authorization", t).contentType(JSON)
        .content("{}")).andExpect(autorizado());
    mvc.perform(post("/api/sedes").header("Authorization", t).contentType(JSON).content("{}"))
        .andExpect(status().isForbidden());
    mvc.perform(put("/api/sedes/AQP").header("Authorization", t).contentType(JSON).content("{}"))
        .andExpect(status().isForbidden());
    mvc.perform(delete("/api/estacionamientos/99999").header("Authorization", t))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminPasaLaAutorizacionEnTodasLasOperaciones() throws Exception {
    String t = bearer(token(Rol.ADMIN));
    mvc.perform(get("/api/sedes").header("Authorization", t)).andExpect(status().isOk());
    // Cuerpo vacío: pasa la autorización y se rechaza por validación (400), sin tocar datos.
    mvc.perform(post("/api/sedes").header("Authorization", t).contentType(JSON).content("{}"))
        .andExpect(status().isBadRequest());
    mvc.perform(post("/api/estacionamientos").header("Authorization", t).contentType(JSON).content("{}"))
        .andExpect(status().isBadRequest());
    mvc.perform(post("/api/estacionamientos/99999/movimientos").header("Authorization", t).contentType(JSON)
        .content("{}")).andExpect(status().isBadRequest());
  }

  @Test
  void metodoNoPrevistoSeNiegaAunConAdmin() throws Exception {
    mvc.perform(patch("/api/sedes/AQP").header("Authorization", bearer(token(Rol.ADMIN))).contentType(JSON)
        .content("{}")).andExpect(status().isForbidden());
  }

  // ---------- CORS ----------

  @Test
  void preflightCorsPermiteSoloElOrigenConfigurado() throws Exception {
    mvc.perform(options("/api/estacionamientos").header("Origin", "http://localhost:4200")
        .header("Access-Control-Request-Method", "GET").header("Access-Control-Request-Headers", "authorization"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    mvc.perform(options("/api/estacionamientos").header("Origin", "http://sitio-ajeno.example")
        .header("Access-Control-Request-Method", "GET")).andExpect(status().isForbidden());
  }
}