package com.seti.franchises.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Producto con más stock por sucursal (incluye sucursal asociada)")
public record ProductWithBranchResponse(
        @Schema(description = "Identificador de la sucursal")
        String branchId,

        @Schema(description = "Nombre de la sucursal")
        String branchName,

        @Schema(description = "Producto con mayor stock en la sucursal")
        ProductResponse product
) {
}
