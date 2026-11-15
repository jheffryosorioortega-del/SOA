package pe.utpvet.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.inventario.model.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
}
