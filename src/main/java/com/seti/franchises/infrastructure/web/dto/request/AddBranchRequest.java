package com.seti.franchises.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para agregar una sucursal a una franquicia.
 * Requisito funcional: "Exponer endpoint para agregar una nueva sucursal a una franquicia."
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear una sucursal")
public class AddBranchRequest {

    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    @Size(min = 1, max = 200)
    @Schema(description = "Nombre de la sucursal", example = "Sucursal Centro", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
