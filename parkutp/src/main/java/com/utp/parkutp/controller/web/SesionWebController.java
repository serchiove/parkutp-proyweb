package com.utp.parkutp.controller.web;

import com.utp.parkutp.model.TokenRespuesta;
import com.utp.parkutp.repository.UsuarioRepository;
import com.utp.parkutp.security.JwtService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SesionWebController {
    private final JwtService jwt;
    private final UsuarioRepository usuarios;
    public SesionWebController(JwtService jwt, UsuarioRepository usuarios) { this.jwt = jwt; this.usuarios = usuarios; }

    @GetMapping("/login")
    public String login() { return "login"; }

    /** Puente entre sesión web y API JWT. El navegador conserva el token solo en memoria. */
    @GetMapping("/sesion/token")
    public ResponseEntity<TokenRespuesta> token(Authentication auth) {
        var usuario = usuarios.findByUsernameIgnoreCase(auth.getName()).filter(u -> u.isActivo())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));
        var respuesta = new TokenRespuesta(jwt.generar(usuario.getUsername(), usuario.getRol()), "Bearer",
            jwt.getExpiracionSegundos(), usuario.getUsername(), usuario.getRol().name());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(respuesta);
    }
}
