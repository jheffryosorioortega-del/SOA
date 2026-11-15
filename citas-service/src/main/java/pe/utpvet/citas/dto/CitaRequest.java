package pe.utpvet.citas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaRequest(
        @Schema(example = "1") @NotNull Integer mascotaId,
        @Schema(example = "1") @NotNull Integer propietarioId,
        @Schema(example = "1") @NotNull Integer veterinarioId,
        @Schema(example = "2026-12-15", description = "Formato AAAA-MM-DD") @NotNull LocalDate fechaCita,
        @Schema(example = "11:00:00", type = "string", description = "Formato HH:MM:SS") @NotNull LocalTime horaInicio,
        @Schema(example = "11:30:00", type = "string", description = "Debe ser mayor que la hora de inicio") @NotNull LocalTime horaFin,
        @Schema(example = "Control anual") @NotBlank String motivo
) {
}
