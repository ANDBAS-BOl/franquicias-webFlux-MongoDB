package com.seti.franchises.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Sucursal")
public record BranchResponse(
        @Schema(description = "Identificador de la sucursal")
        String id,

        @Schema(description = "Nombre de la sucursal")
        String name,

        @Schema(description = "Lista de productos")
        List<ProductResponse> products
) {
}
