package pe.utpvet.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.utpvet.inventario.model.Producto;
import pe.utpvet.inventario.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Catálogo de productos y stock actual")
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final EntityManager entityManager;

    @Operation(summary = "Listar productos con su stock actual")
    @GetMapping
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + id));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        producto.setId(null);
        if (producto.getEstado() == null) {
            producto.setEstado("ACTIVO");
        }
        if (producto.getStockMinimo() == null) {
            producto.setStockMinimo(BigDecimal.ZERO);
        }
        Producto guardado = productoRepository.save(producto);
        entityManager.refresh(guardado); // trae stock_actual/fecha_registro puestos por la BD
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping("/health")
    public String health() {
        return "inventario-service OK";
    }
}
