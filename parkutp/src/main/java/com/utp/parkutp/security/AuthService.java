package com.utp.parkutp.security;

import com.utp.parkutp.dto.LoginDto;
import com.utp.parkutp.entity.UsuarioEntity;
import com.utp.parkutp.exception.NegocioException;
import com.utp.parkutp.model.TokenRespuesta;
import com.utp.parkutp.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
        this.hashFalso = encoder.encode("sin-usuario");
    }

    public TokenRespuesta login(LoginDto dto) {
        var usuario = usuarios.findByUsernameIgnoreCase(dto.getUsername().trim());
        // Se compara siempre contra un hash: el tiempo de respuesta no revela si el usuario existe.
        String hash = usuario.map(UsuarioEntity::getPasswordHash).orElse(hashFalso);
        boolean coincide = encoder.matches(dto.getPassword(), hash);
        if (usuario.isEmpty() || !coincide || !usuario.get().isActivo()) {
            throw new NegocioException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        UsuarioEntity u = usuario.get();
        return new TokenRespuesta(jwt.generar(u.getUsername(), u.getRol()), "Bearer",
                jwt.getExpiracionSegundos(), u.getUsername(), u.getRol().name());
    }
}