package com.utp.parkutp.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.parkutp.services.SedeService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SedeController.class)
class SedeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SedeService sedeService;

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