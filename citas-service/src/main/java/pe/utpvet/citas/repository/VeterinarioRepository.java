package pe.utpvet.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.citas.model.Veterinario;

public interface VeterinarioRepository extends JpaRepository<Veterinario, Integer> {
}
