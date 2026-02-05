package com.seti.franchises.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Franquicia")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FranchiseResponse {

    @Schema(description = "Identificador de la franquicia")
    private String id;

    @Schema(description = "Nombre de la franquicia")
    private String name;

    @Schema(description = "Lista de sucursales")
    private List<BranchResponse> branches;
}
