package com.utp.parkutp;

import com.utp.parkutp.security.JwtService;
import com.utp.parkutp.security.Rol;
import com.utp.parkutp.entity.UsuarioEntity;
import com.utp.parkutp.repository.UsuarioRepository;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@TestConfiguration
public class TokenAdminDePrueba {
    @Bean
    MockMvcBuilderCustomizer tokenAdmin(JwtService jwt, UsuarioRepository usuarios) {
        if (!usuarios.existsByUsernameIgnoreCase("admin-prueba"))
            usuarios.save(new UsuarioEntity("admin-prueba", "no-usado-para-login", Rol.ADMIN));
        String token = jwt.generar("admin-prueba", Rol.ADMIN);
        return builder -> builder.defaultRequest(
                get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin-prueba").roles("ADMIN"))
                    .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()));
    }
}
