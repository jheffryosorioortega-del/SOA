package pe.utpvet.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.citas.model.HistorialEstadoCita;

import java.util.List;

public interface HistorialEstadoCitaRepository extends JpaRepository<HistorialEstadoCita, Long> {
    List<HistorialEstadoCita> findByCitaIdOrderByFechaCambioAsc(Integer citaId);
}
