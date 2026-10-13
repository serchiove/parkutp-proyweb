package com.utp.parkutp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import com.utp.parkutp.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String PREFIJO = "Bearer ";
    private final JwtService jwt;
    private final UsuarioRepository usuarios;

    public JwtAuthenticationFilter(JwtService jwt, UsuarioRepository usuarios) {
        this.jwt = jwt;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            jwt.validar(cabecera.substring(PREFIJO.length()).trim())
                    .ifPresent(claims -> autenticar(claims, request));
        }
        chain.doFilter(request, response);
    }

    private void autenticar(Claims claims, HttpServletRequest request) {
        try {
            String nombre = claims.getSubject();
            String nombreRol = claims.get("rol", String.class);
            if (nombre == null || nombre.isBlank() || nombreRol == null) return;
            Rol rol = Rol.valueOf(nombreRol);
            var usuario = usuarios.findByUsernameIgnoreCase(nombre)
                .filter(u -> u.isActivo() && u.getRol() == rol);
            if (usuario.isEmpty()) return;
            var autenticacion = new UsernamePasswordAuthenticationToken(usuario.get().getUsername(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));
            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
        } catch (JwtException | IllegalArgumentException ex) {
            // Un claim incorrecto no debe provocar un error 500 ni autenticar al usuario.
        }
    }
}
