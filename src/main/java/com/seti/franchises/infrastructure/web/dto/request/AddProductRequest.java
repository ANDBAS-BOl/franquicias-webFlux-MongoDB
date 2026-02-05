package com.seti.franchises.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear un producto")
public record AddProductRequest(
        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(min = 1, max = 200)
        @Schema(description = "Nombre del producto", example = "Producto A", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @Min(value = 0, message = "El stock no puede ser negativo")
        @Schema(description = "Cantidad en stock", example = "10", defaultValue = "0")
        Integer stockQuantity
) {
}
