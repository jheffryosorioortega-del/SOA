package pe.utpvet.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.inventario.model.CategoriaProducto;

public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Integer> {
}
