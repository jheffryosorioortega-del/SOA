package pe.utpvet.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.inventario.model.TipoMovimiento;

import java.util.Optional;

public interface TipoMovimientoRepository extends JpaRepository<TipoMovimiento, Integer> {
    Optional<TipoMovimiento> findByNombre(String nombre);
}
