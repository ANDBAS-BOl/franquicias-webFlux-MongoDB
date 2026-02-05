package com.seti.franchises.infrastructure.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * MongoDB embedded document for a product.
 * Maps to the domain entity Product (name + stock quantity).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("stock_quantity")
    private Integer stockQuantity;
}
