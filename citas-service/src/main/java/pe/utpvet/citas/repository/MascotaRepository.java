package pe.utpvet.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.citas.model.Mascota;

public interface MascotaRepository extends JpaRepository<Mascota, Integer> {
}
