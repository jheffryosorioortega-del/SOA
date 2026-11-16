package pe.utpvet.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Las restricciones de longitud y valores permitidos evitan que datos manipulados lleguen a la base
// de datos y terminen en un error 500 (hallazgo del escaneo OWASP ZAP): ahora responden 400.
public record LoginRequest(
        @Schema(example = "recepcion1", description = "Usuarios de prueba: recepcion1, vet.gomez, jperez, caja1")
        @NotBlank @Size(max = 50) String username,
        @Schema(example = "Vet12345")
        @NotBlank @Size(max = 100) String password,
        @Schema(example = "RECEPCION", allowableValues = {"RECEPCION", "VETERINARIO", "PROPIETARIO", "INVENTARIO_CAJA"})
        @NotBlank @Pattern(regexp = "RECEPCION|VETERINARIO|PROPIETARIO|INVENTARIO_CAJA",
                message = "canal debe ser RECEPCION, VETERINARIO, PROPIETARIO o INVENTARIO_CAJA") String canal
) {
}
