package com.utp.parkutp.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.utp.parkutp.models.Sede;
import com.utp.parkutp.repositories.SedeRepository;

import java.util.List;
import org.junit.jupiter.api.Test;

class SedeServiceTest {

    @Test
    void deberiaRetornarListaVaciaAlIniciar() {
        // Arrange: Instanciamos el repositorio y se lo inyectamos al servicio
        SedeRepository sedeRepository = new SedeRepository();
        SedeService sedeService = new SedeService(sedeRepository);

        // Act: Ejecutamos el método que queremos probar
        List<Sede> sedes = sedeService.obtenerTodas();

        // Assert: Verificamos el resultado esperado
        assertNotNull(sedes, "La lista de sedes no debería ser nula");
        assertEquals(0, sedes.size(), "La lista debería estar vacía inicialmente");
    }

@Test
    void deberiaCrearYValidarObjetoSede() {
        Sede sede = new Sede();
        sede.setId("1");
        sede.setNombre("Sede Arequipa Principal");
        
        assertNotNull(sede);
        assertEquals("1", sede.getId());
        assertEquals("Sede Arequipa Principal", sede.getNombre());
    }
}