package com.seti.franchises.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para actualizar el nombre (franquicia, sucursal o producto).
 * Puntos extra: actualizar nombre de franquicia, sucursal y producto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nuevo nombre")
public class UpdateNameRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 1, max = 200)
    @Schema(description = "Nuevo nombre", example = "Nuevo nombre", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
