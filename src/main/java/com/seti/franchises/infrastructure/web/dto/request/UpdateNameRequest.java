package com.seti.franchises.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Nuevo nombre")
public record UpdateNameRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 1, max = 200)
        @Schema(description = "Nuevo nombre", example = "Nuevo nombre", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
) {
}
