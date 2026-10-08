package com.utp.parkutp;

import com.utp.parkutp.repository.MovimientoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = { "spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop" })
@org.springframework.test.context.TestExecutionListeners({
 org.springframework.test.context.support.DependencyInjectionTestExecutionListener.class,
 org.springframework.test.context.support.DirtiesContextTestExecutionListener.class,
 org.springframework.test.context.transaction.TransactionalTestExecutionListener.class
})
class MovimientoRepositoryTest {
    @org.junit.jupiter.api.BeforeEach
    void relaciones() {
        jdbc.update("INSERT INTO sede(id,nombre) VALUES ('AQP','Arequipa')");
        jdbc.update("INSERT INTO estacionamiento(id,sede_id,nombre,capacidad,activo) VALUES (1,'AQP','Uno',100,true),(2,'AQP','Dos',100,true)");
        jdbc.update("INSERT INTO acceso(id,estacionamiento_id,nombre,tipo) VALUES (1,1,'Entrada uno','ENTRADA'),(2,2,'Entrada dos','ENTRADA')");
    }
    @Autowired
    MovimientoRepository repository;
    @Autowired
    JdbcTemplate jdbc;

    private UUID insertar(long estacionamiento, String fecha, String placa, String tipo) {
        UUID evento = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO movimiento(evento_id,estacionamiento_id,acceso_id,tipo,origen,placa,registrado_en) VALUES (?,?,?,?,?,?,?)",
                evento, estacionamiento, estacionamiento, tipo, "MANUAL", placa, OffsetDateTime.parse(fecha));
        return evento;
    }

    @Test
    void filtrosCombinadosEncuentranUnRegistroAnteriorALosUltimos50() {
        UUID antiguo = insertar(1, "2026-10-06T08:00:00-05:00", "AbC-123", "ENTRADA");
        insertar(1, "2026-10-06T09:00:00-05:00", "ABC-123", "SALIDA");
        insertar(2, "2026-10-06T08:00:00-05:00", "ABC-123", "ENTRADA");
        for (int i = 0; i < 51; i++)
            insertar(1, "2026-10-07T08:00:00-05:00", null, "ENTRADA");
        assertEquals(50, repository.buscarHistorial(1, null, null, null, null, PageRequest.of(0, 50)).size());
        var filas = repository.buscarHistorial(1, OffsetDateTime.parse("2026-10-06T00:00:00-05:00"),
                OffsetDateTime.parse("2026-10-07T00:00:00-05:00"), "ABC", "ENTRADA", PageRequest.of(0, 50));
        assertEquals(1, filas.size());
        assertEquals(antiguo, filas.get(0).toModel().eventoId());
    }

    @Test
    void respetaLosLimitesDelDiaDeArequipa() {
        insertar(1, "2026-10-06T04:59:59Z", "ANTES", "ENTRADA");
        UUID inicio = insertar(1, "2026-10-06T05:00:00Z", "INICIO", "ENTRADA");
        UUID fin = insertar(1, "2026-10-07T04:59:59Z", "FIN", "ENTRADA");
        insertar(1, "2026-10-07T05:00:00Z", "DESPUES", "ENTRADA");
        var filas = repository.buscarHistorial(1, OffsetDateTime.parse("2026-10-06T00:00:00-05:00"),
                OffsetDateTime.parse("2026-10-07T00:00:00-05:00"), null, null, PageRequest.of(0, 50));
        assertEquals(java.util.List.of(fin, inicio), filas.stream().map(m -> m.toModel().eventoId()).toList());
    }

    @Test
    void placaSeBuscaComoFragmentoLiteral() {
        insertar(1, "2026-10-06T08:00:00-05:00", "ABC-123", "ENTRADA");
        insertar(1, "2026-10-06T09:00:00-05:00", null, "ENTRADA");
        for (String texto : java.util.List.of("%", "_", "INEXISTENTE"))
            assertTrue(repository.buscarHistorial(1, null, null, texto, null, PageRequest.of(0, 50)).isEmpty());
        assertEquals(1, repository.buscarHistorial(1, null, null, "ABC", null, PageRequest.of(0, 50)).size());
    }

    @Test
    void eventoSeRecuperaPorUuidYDesempataPorId() {
        UUID primero = insertar(1, "2026-10-06T08:00:00-05:00", null, "ENTRADA");
        UUID segundo = insertar(1, "2026-10-06T08:00:00-05:00", null, "SALIDA");
        assertEquals(primero, repository.findByEventoId(primero).orElseThrow().toModel().eventoId());
        assertTrue(repository.findByEventoId(UUID.randomUUID()).isEmpty());
        var filas = repository.buscarHistorial(1, null, null, null, null, PageRequest.of(0, 50));
        assertEquals(java.util.List.of(segundo, primero), filas.stream().map(m -> m.toModel().eventoId()).toList());
    }
}
