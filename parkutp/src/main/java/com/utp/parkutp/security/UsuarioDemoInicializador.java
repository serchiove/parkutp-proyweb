package com.utp.parkutp.security;

import com.utp.parkutp.entity.UsuarioEntity;
import com.utp.parkutp.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Crea cuentas de demostración solo si DEMO_PASSWORD está definida. */
@Component
public class UsuarioDemoInicializador implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(UsuarioDemoInicializador.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String passwordDemo;

    public UsuarioDemoInicializador(UsuarioRepository usuarios, PasswordEncoder encoder,
            @Value("${parkutp.demo.password}") String passwordDemo) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.passwordDemo = passwordDemo;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (passwordDemo == null || passwordDemo.isBlank()) {
            log.warn("DEMO_PASSWORD no está definida: no se crean cuentas de demostración");
            return;
        }
        crearSiNoExiste("admin", Rol.ADMIN);
        crearSiNoExiste("operador", Rol.OPERADOR);
        crearSiNoExiste("consulta", Rol.CONSULTA);
    }

    private void crearSiNoExiste(String username, Rol rol) {
        if (!usuarios.existsByUsernameIgnoreCase(username)) {
            usuarios.save(new UsuarioEntity(username, encoder.encode(passwordDemo), rol));
            log.info("Cuenta demo creada: {} ({})", username, rol);
        }
    }
}