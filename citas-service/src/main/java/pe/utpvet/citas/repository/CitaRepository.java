package pe.utpvet.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.citas.model.Cita;

import java.time.LocalDate;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Integer> {
    List<Cita> findByFechaCita(LocalDate fechaCita);
    List<Cita> findByVeterinarioId(Integer veterinarioId);
    List<Cita> findByPropietarioId(Integer propietarioId);
}
