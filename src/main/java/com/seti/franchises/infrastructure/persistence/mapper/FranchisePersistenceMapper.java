package com.seti.franchises.infrastructure.persistence.mapper;

import com.seti.franchises.domain.entity.Branch;
import com.seti.franchises.domain.entity.Franchise;
import com.seti.franchises.domain.entity.Product;
import com.seti.franchises.infrastructure.persistence.document.BranchDocument;
import com.seti.franchises.infrastructure.persistence.document.FranchiseDocument;
import com.seti.franchises.infrastructure.persistence.document.ProductDocument;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mapper between domain entities and MongoDB documents.
 * Keeps domain and infrastructure decoupled.
 */
@Component
public class FranchisePersistenceMapper {

    public FranchiseDocument toDocument(Franchise franchise) {
        if (franchise == null) {
            return null;
        }
        return FranchiseDocument.builder()
                .id(franchise.getId())
                .name(franchise.getName())
                .branches(Optional.ofNullable(franchise.getBranches())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(this::toBranchDocument)
                        .collect(Collectors.toList()))
                .build();
    }

    public Franchise toEntity(FranchiseDocument document) {
        if (document == null) {
            return null;
        }
        return Franchise.builder()
                .id(document.getId())
                .name(document.getName())
                .branches(Optional.ofNullable(document.getBranches())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(this::toBranchEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    public BranchDocument toBranchDocument(Branch branch) {
        if (branch == null) {
            return null;
        }
        return BranchDocument.builder()
                .id(branch.getId())
                .name(branch.getName())
                .products(Optional.ofNullable(branch.getProducts())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(this::toProductDocument)
                        .collect(Collectors.toList()))
                .build();
    }

    public Branch toBranchEntity(BranchDocument document) {
        if (document == null) {
            return null;
        }
        return Branch.builder()
                .id(document.getId())
                .name(document.getName())
                .products(Optional.ofNullable(document.getProducts())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(this::toProductEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    public ProductDocument toProductDocument(Product product) {
        if (product == null) {
            return null;
        }
        return ProductDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .stockQuantity(product.getStockQuantity())
                .build();
    }

    public Product toProductEntity(ProductDocument document) {
        if (document == null) {
            return null;
        }
        return Product.builder()
                .id(document.getId())
                .name(document.getName())
                .stockQuantity(document.getStockQuantity())
                .build();
    }
}
