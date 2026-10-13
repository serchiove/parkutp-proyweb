package com.utp.parkutp.security;

import com.utp.parkutp.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/** Una sesión existente no conserva permisos de un usuario desactivado o cambiado. */
public class UsuarioSesionFilter extends OncePerRequestFilter {
    private final UsuarioRepository usuarios;
    public UsuarioSesionFilter(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var contexto = SecurityContextHolder.getContext();
        var auth = contexto.getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            var usuario = usuarios.findByUsernameIgnoreCase(auth.getName()).filter(u -> u.isActivo());
            if (usuario.isEmpty()) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            } else {
                var actual = usuario.get();
                var vigente = new UsernamePasswordAuthenticationToken(actual.getUsername(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + actual.getRol().name())));
                vigente.setDetails(auth.getDetails());
                contexto.setAuthentication(vigente);
            }
        }
        chain.doFilter(request, response);
    }
}
