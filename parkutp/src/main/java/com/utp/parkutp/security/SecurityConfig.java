package com.utp.parkutp.security;

import com.utp.parkutp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return nombre -> usuarios.findByUsernameIgnoreCase(nombre.trim())
            .map(u -> User.withUsername(u.getUsername()).password(u.getPasswordHash())
                .roles(u.getRol().name()).disabled(!u.isActivo()).build())
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
    }

    @Bean
    @Order(1)
    public SecurityFilterChain cadenaApi(HttpSecurity http, JwtService jwt, UsuarioRepository usuarios,
            @org.springframework.beans.factory.annotation.Qualifier("corsConfigurationSource")
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.securityMatcher(antMatcher("/api/**"))
            .csrf(csrf -> csrf.disable())
            .cors(c -> c.configurationSource(corsConfigurationSource))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtAuthenticationFilter(jwt, usuarios), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(e -> e.authenticationEntryPoint(new ApiAuthenticationEntryPoint())
                .accessDeniedHandler(new ApiAccessDeniedHandler()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(antMatcher(HttpMethod.OPTIONS, "/api/**")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.POST, "/api/auth/login")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.GET, "/api/auth/yo")).authenticated()
                .requestMatchers(antMatcher(HttpMethod.POST, "/api/estacionamientos/*/movimientos"))
                    .hasAnyRole("ADMIN", "OPERADOR")
                .requestMatchers(antMatcher(HttpMethod.GET, "/api/**")).hasAnyRole("ADMIN", "OPERADOR", "CONSULTA")
                .requestMatchers(antMatcher(HttpMethod.POST, "/api/**"), antMatcher(HttpMethod.PUT, "/api/**"),
                    antMatcher(HttpMethod.DELETE, "/api/**")).hasRole("ADMIN")
                .anyRequest().denyAll());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain cadenaVistas(HttpSecurity http, UsuarioRepository usuarios) throws Exception {
        // La web usa sesión y CSRF. La API anterior sigue exigiendo Bearer JWT.
        http.addFilterBefore(new UsuarioSesionFilter(usuarios), AuthorizationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Los forwards internos renderizan JSP tras autorizar la ruta pública original.
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD, jakarta.servlet.DispatcherType.INCLUDE,
                    jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers(antMatcher("/login"), antMatcher("/css/**"), antMatcher("/js/**"),
                    antMatcher("/webjars/**"), antMatcher("/error"), antMatcher("/jakarta.faces.resource/**"),
                    antMatcher("/jsf/jakarta.faces.resource/**")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.GET, "/sesion/token")).authenticated()
                .requestMatchers(antMatcher(HttpMethod.GET, "/"), antMatcher(HttpMethod.GET, "/estacionamientos"),
                    antMatcher(HttpMethod.GET, "/estacionamientos/*/operacion"),
                    antMatcher(HttpMethod.GET, "/jsf/sedes.xhtml")).hasAnyRole("ADMIN", "OPERADOR", "CONSULTA")
                .requestMatchers(antMatcher("/estacionamientos/**"), antMatcher("/jsf/**")).hasRole("ADMIN")
                .anyRequest().denyAll())
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/estacionamientos", true)
                .failureUrl("/login?error").permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?salida").invalidateHttpSession(true)
                .deleteCookies("JSESSIONID").permitAll())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> {
                    if (antMatcher("/sesion/token").matches(req)) res.sendError(401);
                    else new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/login")
                        .commence(req, res, ex);
                })
                .accessDeniedHandler((req, res, ex) -> {
                    res.setStatus(403);
                    req.getRequestDispatcher("/WEB-INF/views/acceso-denegado.jsp").forward(req, res);
                }));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${CORS_ORIGENES:http://localhost:4200}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim)
            .filter(o -> !o.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
    }
}
