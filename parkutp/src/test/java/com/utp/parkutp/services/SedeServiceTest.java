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
}


