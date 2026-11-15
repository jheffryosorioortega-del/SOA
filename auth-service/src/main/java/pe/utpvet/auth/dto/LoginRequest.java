package pe.utpvet.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "recepcion1", description = "Usuarios de prueba: recepcion1, vet.gomez, jperez, caja1")
        @NotBlank String username,
        @Schema(example = "Vet12345")
        @NotBlank String password,
        @Schema(example = "RECEPCION", allowableValues = {"RECEPCION", "VETERINARIO", "PROPIETARIO", "INVENTARIO_CAJA"})
        @NotBlank String canal
) {
}
