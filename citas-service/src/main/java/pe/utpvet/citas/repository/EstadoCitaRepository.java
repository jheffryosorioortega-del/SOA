package pe.utpvet.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.citas.model.EstadoCita;

import java.util.Optional;

public interface EstadoCitaRepository extends JpaRepository<EstadoCita, Integer> {
    Optional<EstadoCita> findByNombre(String nombre);
}
