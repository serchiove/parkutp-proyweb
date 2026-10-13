package com.utp.parkutp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
        public SecurityFilterChain cadenaApi(HttpSecurity http, JwtService jwt,
            @Qualifier("corsConfigurationSource") CorsConfigurationSource cors) throws Exception {
        http.securityMatcher(antMatcher("/api/**"))
                .csrf(csrf -> csrf.disable())
                .cors(c -> c.configurationSource(cors))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(jwt), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new ApiAuthenticationEntryPoint())
                        .accessDeniedHandler(new ApiAccessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(antMatcher(HttpMethod.OPTIONS, "/api/**")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/auth/login")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/auth/yo")).authenticated()
                                                // Registrar entrada o salida: ADMIN y OPERADOR.
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/estacionamientos/*/movimientos"))
                                .hasAnyRole(Rol.ADMIN.name(), Rol.OPERADOR.name())
                        // Consultas (sedes, zonas, accesos, aforo e historial): los tres roles //
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/**"))
                                .hasAnyRole(Rol.ADMIN.name(), Rol.OPERADOR.name(), Rol.CONSULTA.name())
                        // Crear, editar o eliminar sedes, zonas y accesos: solo ADMIN //
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/**")).hasRole(Rol.ADMIN.name())
                        .requestMatchers(antMatcher(HttpMethod.PUT, "/api/**")).hasRole(Rol.ADMIN.name())
                        .requestMatchers(antMatcher(HttpMethod.DELETE, "/api/**")).hasRole(Rol.ADMIN.name())
                        // Todo lo no previsto se niega //
                        .anyRequest().denyAll());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain cadenaVistas(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${CORS_ORIGENES:http://localhost:4200}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenes.split(","))
                .map(String::trim).filter(o -> !o.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
    }
}