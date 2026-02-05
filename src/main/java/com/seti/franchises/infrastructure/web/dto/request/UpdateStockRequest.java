package com.seti.franchises.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Nuevo valor de stock para el producto")
public record UpdateStockRequest(
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        @Schema(description = "Cantidad en stock", example = "25", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer stockQuantity
) {
}
