package pe.utpvet.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.auth.model.Sesion;

import java.util.Optional;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
    Optional<Sesion> findByToken(String token);
}
