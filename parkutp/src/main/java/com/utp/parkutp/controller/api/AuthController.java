package com.utp.parkutp.controller.api;

import com.utp.parkutp.dto.LoginDto;
import com.utp.parkutp.model.TokenRespuesta;
import com.utp.parkutp.security.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
}