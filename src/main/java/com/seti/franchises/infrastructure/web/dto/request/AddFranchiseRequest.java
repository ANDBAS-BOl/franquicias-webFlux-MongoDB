package com.seti.franchises.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear una franquicia")
public record AddFranchiseRequest(
        @NotBlank(message = "El nombre de la franquicia es obligatorio")
        @Size(min = 1, max = 200)
        @Schema(description = "Nombre de la franquicia", example = "Franquicia Norte", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
) {
}
