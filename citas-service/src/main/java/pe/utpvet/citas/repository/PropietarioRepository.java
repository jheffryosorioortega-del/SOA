package pe.utpvet.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.citas.model.Propietario;

public interface PropietarioRepository extends JpaRepository<Propietario, Integer> {
}
