package pe.utpvet.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.citas.model.HorarioVeterinario;

import java.util.List;

public interface HorarioVeterinarioRepository extends JpaRepository<HorarioVeterinario, Integer> {
    List<HorarioVeterinario> findByVeterinarioId(Integer veterinarioId);
}
