package com.utp.parkutp;

import com.utp.parkutp.security.JwtService;
import com.utp.parkutp.security.Rol;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@TestConfiguration
public class TokenAdminDePrueba {
    @Bean
    MockMvcBuilderCustomizer tokenAdmin(JwtService jwt) {
        String token = jwt.generar("admin-prueba", Rol.ADMIN);
        return builder -> builder.defaultRequest(
                get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }
}