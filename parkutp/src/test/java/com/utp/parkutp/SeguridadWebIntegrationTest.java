package com.utp.parkutp;

import com.utp.parkutp.entity.UsuarioEntity;
import com.utp.parkutp.repository.UsuarioRepository;
import com.utp.parkutp.security.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named="TEST_DB_URL", matches=".*")
@TestPropertySource(properties={"parkutp.jwt.secret=clave-web-de-pruebas-con-mas-de-32-caracteres", "parkutp.demo.password="})
@TestExecutionListeners({ServletTestExecutionListener.class, DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
class SeguridadWebIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    private static final String CLAVE = "ClaveWebDePruebas";

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
    void preparar() {
        jdbc.execute("TRUNCATE movimiento,dispositivo,acceso,estacionamiento,sede,usuario RESTART IDENTITY CASCADE");
        for (Rol rol : Rol.values()) usuarios.save(new UsuarioEntity(rol.name().toLowerCase(), encoder.encode(CLAVE), rol));
        jdbc.update("INSERT INTO sede(id,nombre) VALUES ('WEB','Sede web')");
    }

    private MockHttpSession login(String nombre) throws Exception {
        var resultado = mvc.perform(post("/login").with(csrf()).param("username", nombre).param("password", CLAVE))
            .andExpect(redirectedUrl("/estacionamientos")).andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    @Test void anonimoDebeIniciarSesionYNoPuedeObtenerJwt() throws Exception {
        mvc.perform(get("/estacionamientos")).andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/sesion/token")).andExpect(status().isUnauthorized());
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
    }

    @Test void loginIncorrectoOInactivoSeRechaza() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", "incorrecta"))
            .andExpect(redirectedUrl("/login?error"));
        jdbc.update("UPDATE usuario SET activo=false WHERE username='admin'");
        mvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", CLAVE))
            .andExpect(redirectedUrl("/login?error"));
    }

    @Test void formulariosExigenCsrfInclusoParaAdmin() throws Exception {
        var sesion = login("admin");
        mvc.perform(post("/estacionamientos").session(sesion).param("sedeId", "WEB").param("nombre", "Zona").param("capacidad", "2"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/jsf/sedes.xhtml").session(sesion)).andExpect(status().isForbidden());
        mvc.perform(post("/logout").session(sesion)).andExpect(status().isForbidden());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM estacionamiento", Integer.class));
    }

    @Test void adminPuedeCrearDesdeFormularioConCsrf() throws Exception {
        mvc.perform(post("/estacionamientos").session(login("admin")).with(csrf())
            .param("sedeId", "WEB").param("nombre", "Zona").param("capacidad", "2").param("activo", "true"))
            .andExpect(redirectedUrl("/estacionamientos"));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM estacionamiento", Integer.class));
        assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM acceso", Integer.class));
    }

    @Test void operadorYConsultaNoAdministranPorJspNiJsf() throws Exception {
        for (String nombre : new String[]{"operador", "consulta"}) {
            var sesion = login(nombre);
            mvc.perform(get("/estacionamientos").session(sesion)).andExpect(status().isOk());
            mvc.perform(get("/estacionamientos/nuevo").session(sesion)).andExpect(status().isForbidden());
            mvc.perform(get("/estacionamientos/1/editar").session(sesion)).andExpect(status().isForbidden());
            mvc.perform(get("/estacionamientos/1/eliminar").session(sesion)).andExpect(status().isForbidden());
            mvc.perform(post("/estacionamientos").session(sesion).with(csrf())).andExpect(status().isForbidden());
            mvc.perform(post("/estacionamientos/1").session(sesion).with(csrf())).andExpect(status().isForbidden());
            mvc.perform(post("/estacionamientos/1/eliminar").session(sesion).with(csrf())).andExpect(status().isForbidden());
            mvc.perform(post("/jsf/sedes.xhtml").session(sesion).with(csrf())).andExpect(status().isForbidden());
        }
    }

    @Test void panelObtieneJwtDesdeSesionYApiNoAceptaSoloCookie() throws Exception {
        var sesion = login("operador");
        String respuesta = mvc.perform(get("/sesion/token").session(sesion)).andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(jsonPath("$.rol").value("OPERADOR")).andReturn().getResponse().getContentAsString();
        String jwt = new com.fasterxml.jackson.databind.ObjectMapper().readTree(respuesta).get("token").asText();
        mvc.perform(get("/api/sedes").session(sesion)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/sedes").header("Authorization", "Bearer " + jwt)).andExpect(status().isOk());
        mvc.perform(post("/api/sedes").header("Authorization", "Bearer " + jwt).contentType("application/json").content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test void logoutInvalidaSesionYExigeNuevoLogin() throws Exception {
        var sesion = login("admin");
        mvc.perform(post("/logout").session(sesion).with(csrf())).andExpect(redirectedUrl("/login?salida"));
        assertTrue(sesion.isInvalid());
        mvc.perform(get("/sesion/token")).andExpect(status().isUnauthorized());
    }

    @Test void desactivarUsuarioInvalidaLaSesionExistente() throws Exception {
        var sesion = login("admin");
        jdbc.update("UPDATE usuario SET activo=false WHERE username='admin'");
        mvc.perform(get("/sesion/token").session(sesion)).andExpect(status().isUnauthorized());
        assertTrue(sesion.isInvalid());
    }

    @Test void cambiarRolActualizaPermisosDeLaSesion() throws Exception {
        var sesion = login("admin");
        jdbc.update("UPDATE usuario SET rol='CONSULTA' WHERE username='admin'");
        mvc.perform(get("/estacionamientos/nuevo").session(sesion)).andExpect(status().isForbidden());
        mvc.perform(get("/sesion/token").session(sesion)).andExpect(status().isOk())
            .andExpect(jsonPath("$.rol").value("CONSULTA"));
    }
}
