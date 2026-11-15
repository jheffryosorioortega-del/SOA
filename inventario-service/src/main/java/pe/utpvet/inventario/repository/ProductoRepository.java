package pe.utpvet.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.inventario.model.Producto;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    Optional<Producto> findByCodigo(String codigo);
}
