package pe.utpvet.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utpvet.auth.model.Usuario;
import pe.utpvet.auth.repository.UsuarioRepository;

/**
 * Los usuarios de 02_seed.sql se cargan con hashes de marcador de posición
 * ("hash_demo_1", etc.) porque no existe una forma segura de precomputar un
 * hash BCrypt fuera de la aplicación. Al arrancar, esta clase los reemplaza
 * por un hash BCrypt real de la contraseña de prueba (seed.default-password),
 * usando el mismo PasswordEncoder que usa el login.
 */
@Component
@RequiredArgsConstructor
public class SeedPasswordInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.default-password}")
    private String defaultPassword;

    @Override
    @Transactional
    public void run(String... args) {
        for (Usuario usuario : usuarioRepository.findAll()) {
            if (usuario.getPasswordHash() != null && usuario.getPasswordHash().startsWith("hash_demo_")) {
                usuario.setPasswordHash(passwordEncoder.encode(defaultPassword));
                usuarioRepository.save(usuario);
            }
        }
    }
}
