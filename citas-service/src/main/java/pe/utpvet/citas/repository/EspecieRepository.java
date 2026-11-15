package pe.utpvet.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utpvet.citas.model.Especie;

public interface EspecieRepository extends JpaRepository<Especie, Integer> {
}
