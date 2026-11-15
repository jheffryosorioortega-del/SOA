package pe.utpvet.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.inventario.model.AlertaStock;

import java.util.List;

public interface AlertaStockRepository extends JpaRepository<AlertaStock, Long> {
    List<AlertaStock> findByEstado(String estado);
}
