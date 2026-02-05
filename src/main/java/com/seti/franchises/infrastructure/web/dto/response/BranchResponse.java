package com.seti.franchises.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Sucursal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {

    @Schema(description = "Identificador de la sucursal")
    private String id;

    @Schema(description = "Nombre de la sucursal")
    private String name;

    @Schema(description = "Lista de productos")
    private List<ProductResponse> products;
}
