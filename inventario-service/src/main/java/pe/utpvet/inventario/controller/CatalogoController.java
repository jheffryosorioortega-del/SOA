package pe.utpvet.inventario.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.utpvet.inventario.model.CategoriaProducto;
import pe.utpvet.inventario.model.Proveedor;
import pe.utpvet.inventario.model.TipoMovimiento;
import pe.utpvet.inventario.model.UnidadMedida;
import pe.utpvet.inventario.repository.CategoriaProductoRepository;
import pe.utpvet.inventario.repository.ProveedorRepository;
import pe.utpvet.inventario.repository.TipoMovimientoRepository;
import pe.utpvet.inventario.repository.UnidadMedidaRepository;

import java.util.List;

/** Catálogos de referencia usados al crear productos/movimientos. */
@RestController
@RequiredArgsConstructor
public class CatalogoController {

    private final CategoriaProductoRepository categoriaProductoRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final ProveedorRepository proveedorRepository;

    @GetMapping("/api/categorias-producto")
    public List<CategoriaProducto> categorias() {
        return categoriaProductoRepository.findAll();
    }

    @GetMapping("/api/unidades-medida")
    public List<UnidadMedida> unidades() {
        return unidadMedidaRepository.findAll();
    }

    @GetMapping("/api/tipos-movimiento")
    public List<TipoMovimiento> tiposMovimiento() {
        return tipoMovimientoRepository.findAll();
    }

    @GetMapping("/api/proveedores")
    public List<Proveedor> proveedores() {
        return proveedorRepository.findAll();
    }
}
