package com.seti.franchises.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Producto")
public record ProductResponse(
        @Schema(description = "Identificador del producto")
        String id,

        @Schema(description = "Nombre del producto")
        String name,

        @Schema(description = "Cantidad en stock")
        Integer stockQuantity,

        @Schema(description = "Indica si el producto está habilitado (false = borrado lógico)")
        boolean enabled
) {
}
