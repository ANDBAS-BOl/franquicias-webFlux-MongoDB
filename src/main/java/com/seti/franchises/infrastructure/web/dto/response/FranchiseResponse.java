package com.seti.franchises.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Franquicia")
public record FranchiseResponse(
        @Schema(description = "Identificador de la franquicia")
        String id,

        @Schema(description = "Nombre de la franquicia")
        String name,

        @Schema(description = "Lista de sucursales")
        List<BranchResponse> branches
) {
}
