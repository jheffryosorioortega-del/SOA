package pe.utpvet.citas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CambiarEstadoRequest(
        @Schema(example = "CONFIRMADA",
                allowableValues = {"PROGRAMADA", "CONFIRMADA", "REPROGRAMADA", "CANCELADA", "ATENDIDA", "NO_ASISTIO"})
        @NotBlank String estado,
        @Schema(example = "Confirmada por teléfono") String observacion
) {
}
