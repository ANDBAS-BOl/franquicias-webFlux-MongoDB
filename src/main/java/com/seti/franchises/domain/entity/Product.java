package com.seti.franchises.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing a product offered in a branch.
 * Aligns with the requirement: "Un producto se compone de un nombre y una cantidad de stock."
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String id;
    private String name;
    private Integer stockQuantity;
}
