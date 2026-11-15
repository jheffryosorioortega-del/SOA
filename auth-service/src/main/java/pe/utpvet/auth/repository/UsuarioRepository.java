package pe.utpvet.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.auth.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);
}
