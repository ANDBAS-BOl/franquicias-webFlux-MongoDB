package com.seti.franchises.infrastructure.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB embedded document for a branch (sucursal).
 * Maps to the domain entity Branch (name + list of products).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDocument {

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("products")
    @Builder.Default
    private List<ProductDocument> products = new ArrayList<>();
}
