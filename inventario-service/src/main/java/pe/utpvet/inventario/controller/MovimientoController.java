package pe.utpvet.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.utpvet.inventario.dto.MovimientoRequest;
import pe.utpvet.inventario.model.MovimientoInventario;
import pe.utpvet.inventario.repository.MovimientoInventarioRepository;
import pe.utpvet.inventario.service.InventarioService;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@Tag(name = "Movimientos de inventario", description = "Kárdex: entradas y salidas de stock")
public class MovimientoController {

    private final InventarioService inventarioService;
    private final MovimientoInventarioRepository movimientoRepository;

    @Operation(summary = "Listar todos los movimientos")
    @GetMapping
    public List<MovimientoInventario> listar() {
        return movimientoRepository.findAll();
    }

    @Operation(summary = "Movimientos de un producto", description = "Kárdex de un producto, del más reciente al más antiguo.")
    @GetMapping("/producto/{productoId}")
    public List<MovimientoInventario> porProducto(@PathVariable Integer productoId) {
        return movimientoRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId);
    }

    @Operation(summary = "Registrar una entrada o salida de stock",
            description = "Actualiza el stock del producto. Si se envía citaId, Inventario consulta al servicio de Citas para comprobar que la cita existe (404 si no existe, 503 si Citas no responde). Responde 409 si la salida dejaría el stock negativo. Si el stock queda bajo el mínimo, se crea una alerta.")
    @PostMapping
    public ResponseEntity<MovimientoInventario> registrar(@Valid @RequestBody MovimientoRequest request,
                                                            HttpServletRequest http) {
        Integer usuarioId = (Integer) http.getAttribute("usuarioId");
        MovimientoInventario creado = inventarioService.registrarMovimiento(
                request, usuarioId, http.getHeader("Authorization"));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
