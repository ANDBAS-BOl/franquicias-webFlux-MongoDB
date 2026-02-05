package com.seti.franchises.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO para "producto con más stock por sucursal".
 * Requisito: "Debe retornar un listado de productos que indique a qué sucursal pertenece."
 */
@Schema(description = "Producto con más stock por sucursal (incluye sucursal asociada)")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithBranchResponse {

    @Schema(description = "Identificador de la sucursal")
    private String branchId;

    @Schema(description = "Nombre de la sucursal")
    private String branchName;

    @Schema(description = "Producto con mayor stock en la sucursal")
    private ProductResponse product;
}
