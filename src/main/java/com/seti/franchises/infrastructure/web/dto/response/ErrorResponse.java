package com.seti.franchises.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Respuesta de error de la API")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    @Schema(description = "Código HTTP")
    private int status;

    @Schema(description = "Mensaje de error")
    private String message;
}
