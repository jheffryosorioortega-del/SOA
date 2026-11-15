package pe.utpvet.inventario.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utpvet.inventario.dto.MovimientoRequest;
import pe.utpvet.inventario.model.MovimientoInventario;
import pe.utpvet.inventario.model.Producto;
import pe.utpvet.inventario.model.Proveedor;
import pe.utpvet.inventario.model.TipoMovimiento;
import pe.utpvet.inventario.repository.MovimientoInventarioRepository;
import pe.utpvet.inventario.repository.ProductoRepository;
import pe.utpvet.inventario.repository.ProveedorRepository;
import pe.utpvet.inventario.repository.TipoMovimientoRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProveedorRepository proveedorRepository;
    private final EntityManager entityManager;

    @Transactional
    public MovimientoInventario registrarMovimiento(MovimientoRequest request, Integer usuarioId) {
        Producto producto = productoRepository.findById(request.productoId())
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + request.productoId()));
        TipoMovimiento tipo = tipoMovimientoRepository.findByNombre(request.tipoMovimiento())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de movimiento inválido: " + request.tipoMovimiento()));

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(request.cantidad());
        movimiento.setCitaId(request.citaId());
        movimiento.setUsuarioId(usuarioId);
        movimiento.setObservacion(request.observacion());

        if (request.proveedorId() != null) {
            Proveedor proveedor = proveedorRepository.findById(request.proveedorId())
                    .orElseThrow(() -> new NoSuchElementException("Proveedor no encontrado: " + request.proveedorId()));
            movimiento.setProveedor(proveedor);
        }

        // El trigger fn_aplicar_movimiento de la base de datos actualiza
        // productos.stock_actual, calcula saldo_resultante, rechaza el
        // movimiento si dejaría stock negativo, y genera una fila en
        // alertas_stock si el stock cae bajo el mínimo. Como esos cambios
        // los aplica el trigger (no Hibernate), se refresca el movimiento y
        // el producto para que la respuesta refleje los valores reales.
        MovimientoInventario guardado = movimientoRepository.save(movimiento);
        entityManager.refresh(guardado);
        entityManager.refresh(producto);
        return guardado;
    }
}
