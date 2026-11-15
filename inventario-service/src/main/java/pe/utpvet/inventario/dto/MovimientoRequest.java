package pe.utpvet.inventario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MovimientoRequest(
        @Schema(example = "1") @NotNull Integer productoId,
        @Schema(example = "ENTRADA_COMPRA", allowableValues = {"ENTRADA_COMPRA", "SALIDA_CONSUMO", "AJUSTE", "DEVOLUCION"})
        @NotBlank String tipoMovimiento,
        @Schema(example = "5", description = "Mayor que 0")
        @NotNull @DecimalMin(value = "0.01") BigDecimal cantidad,
        @Schema(description = "Proveedor, cuando es una entrada por compra") Integer proveedorId,
        @Schema(description = "Cita donde se consumió el producto") Integer citaId,
        @Schema(example = "Reposición de stock") String observacion
) {
}
