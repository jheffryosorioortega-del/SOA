package pe.utpvet.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.inventario.model.UnidadMedida;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Integer> {
}
