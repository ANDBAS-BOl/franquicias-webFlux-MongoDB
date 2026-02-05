package com.seti.franchises.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear una sucursal")
public record AddBranchRequest(
        @NotBlank(message = "El nombre de la sucursal es obligatorio")
        @Size(min = 1, max = 200)
        @Schema(description = "Nombre de la sucursal", example = "Sucursal Centro", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
) {
}
