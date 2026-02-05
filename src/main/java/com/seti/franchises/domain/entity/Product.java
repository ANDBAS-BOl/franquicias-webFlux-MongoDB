package com.seti.franchises.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing a product offered in a branch.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String id;
    private String name;
    private Integer stockQuantity;

    /**
     * Indica si el producto está habilitado. Por defecto true.
     * false = borrado lógico (soft delete). Recomendado en entornos productivos.
     */
    @Builder.Default
    private Boolean enabled = true;
}
