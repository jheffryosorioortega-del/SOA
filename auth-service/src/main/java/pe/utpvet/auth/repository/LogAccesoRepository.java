package pe.utpvet.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.auth.model.LogAcceso;

public interface LogAccesoRepository extends JpaRepository<LogAcceso, Long> {
}
