package com.utp.parkutp.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SedeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deberiaRetornarStatus200AlListarSedes() throws Exception {
        mockMvc.perform(get("/api/sedes"))
                .andExpect(status().isOk());
    }

    @Test
    void deberiaRetornarStatus404ParaRutaInexistente() throws Exception {
        mockMvc.perform(get("/api/sedes/inexistente"))
                .andExpect(status().isNotFound());
    }
}