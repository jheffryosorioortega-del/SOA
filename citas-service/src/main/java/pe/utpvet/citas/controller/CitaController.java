package pe.utpvet.citas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.utpvet.citas.dto.CambiarEstadoRequest;
import pe.utpvet.citas.dto.CitaRequest;
import pe.utpvet.citas.model.Cita;
import pe.utpvet.citas.model.HistorialEstadoCita;
import pe.utpvet.citas.service.CitaService;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
@Tag(name = "Citas", description = "Programación y seguimiento de citas veterinarias")
public class CitaController {

    private final CitaService citaService;

    @Operation(summary = "Listar citas")
    @GetMapping
    public List<Cita> listar() {
        return citaService.listar();
    }

    @Operation(summary = "Consultar una cita por id")
    @GetMapping("/{id}")
    public Cita obtener(@PathVariable Integer id) {
        return citaService.obtener(id);
    }

    @Operation(summary = "Historial de estados de una cita",
            description = "Devuelve, en orden cronológico, los cambios de estado de la cita.")
    @GetMapping("/{id}/historial")
    public List<HistorialEstadoCita> historial(@PathVariable Integer id) {
        return citaService.historial(id);
    }

    @Operation(summary = "Programar una cita",
            description = "Crea la cita en estado PROGRAMADA. Responde 409 si el veterinario ya tiene otra cita activa que se cruza en ese horario.")
    @PostMapping
    public ResponseEntity<Cita> crear(@Valid @RequestBody CitaRequest request, HttpServletRequest http) {
        Integer usuarioId = (Integer) http.getAttribute("usuarioId");
        Cita creada = citaService.crear(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @Operation(summary = "Cambiar el estado de una cita",
            description = "Confirma, reprograma, cancela o marca como atendida. El cambio queda en el historial.")
    @PatchMapping("/{id}/estado")
    public Cita cambiarEstado(@PathVariable Integer id,
                               @Valid @RequestBody CambiarEstadoRequest request,
                               HttpServletRequest http) {
        Integer usuarioId = (Integer) http.getAttribute("usuarioId");
        return citaService.cambiarEstado(id, request.estado(), request.observacion(), usuarioId);
    }

    @GetMapping("/health")
    public String health() {
        return "citas-service OK";
    }
}
