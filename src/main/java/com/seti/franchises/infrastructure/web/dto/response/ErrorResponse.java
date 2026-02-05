package com.seti.franchises.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de error de la API")
public record ErrorResponse(
        @Schema(description = "Código HTTP")
        int status,

        @Schema(description = "Mensaje de error")
        String message
) {
}
