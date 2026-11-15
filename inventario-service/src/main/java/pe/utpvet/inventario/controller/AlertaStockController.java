package pe.utpvet.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.utpvet.inventario.model.AlertaStock;
import pe.utpvet.inventario.repository.AlertaStockRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas de stock", description = "Avisos cuando un producto baja de su stock mínimo")
public class AlertaStockController {

    private final AlertaStockRepository alertaStockRepository;

    @Operation(summary = "Listar alertas pendientes")
    @GetMapping
    public List<AlertaStock> listarPendientes() {
        return alertaStockRepository.findByEstado("PENDIENTE");
    }

    @Operation(summary = "Marcar una alerta como atendida")
    @PatchMapping("/{id}/atender")
    public AlertaStock atender(@PathVariable Long id) {
        AlertaStock alerta = alertaStockRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerta no encontrada: " + id));
        alerta.setEstado("ATENDIDA");
        alerta.setFechaAtencion(OffsetDateTime.now());
        return alertaStockRepository.save(alerta);
    }
}
