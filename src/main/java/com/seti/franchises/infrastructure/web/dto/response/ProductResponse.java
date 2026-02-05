package com.seti.franchises.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Producto")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    @Schema(description = "Identificador del producto")
    private String id;

    @Schema(description = "Nombre del producto")
    private String name;

    @Schema(description = "Cantidad en stock")
    private Integer stockQuantity;
}
