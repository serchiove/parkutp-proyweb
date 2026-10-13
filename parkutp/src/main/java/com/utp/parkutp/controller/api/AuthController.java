package com.utp.parkutp.controller.api;

import com.utp.parkutp.dto.LoginDto;
import com.utp.parkutp.model.TokenRespuesta;
import com.utp.parkutp.security.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public TokenRespuesta login(@Valid @RequestBody LoginDto dto) {
        return auth.login(dto);
    }

    /** Devuelve quién es el dueño del token. Sin token válido responde 401. */
    @GetMapping("/yo")
    public Map<String, String> yo(Authentication autenticacion) {
        String rol = autenticacion.getAuthorities().stream().findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", "")).orElse("");
        return Map.of("usuario", autenticacion.getName(), "rol", rol);
    }
}