package com.seti.franchises.infrastructure.web.mapper;

import com.seti.franchises.application.service.FranchiseUseCaseService;
import com.seti.franchises.domain.entity.Branch;
import com.seti.franchises.domain.entity.Franchise;
import com.seti.franchises.domain.entity.Product;
import com.seti.franchises.infrastructure.web.dto.response.BranchResponse;
import com.seti.franchises.infrastructure.web.dto.response.FranchiseResponse;
import com.seti.franchises.infrastructure.web.dto.response.ProductResponse;
import com.seti.franchises.infrastructure.web.dto.response.ProductWithBranchResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ApiMapper {

    public FranchiseResponse toFranchiseResponse(Franchise franchise) {
        if (franchise == null) return null;
        return FranchiseResponse.builder()
                .id(franchise.getId())
                .name(franchise.getName())
                .branches(Optional.ofNullable(franchise.getBranches())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(this::toBranchResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public BranchResponse toBranchResponse(Branch branch) {
        if (branch == null) return null;
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .products(Optional.ofNullable(branch.getProducts())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(this::toProductResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public ProductResponse toProductResponse(Product product) {
        if (product == null) return null;
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .stockQuantity(product.getStockQuantity())
                .build();
    }

    public ProductWithBranchResponse toProductWithBranchResponse(FranchiseUseCaseService.ProductWithBranchDto dto) {
        if (dto == null) return null;
        return ProductWithBranchResponse.builder()
                .branchId(dto.getBranchId())
                .branchName(dto.getBranchName())
                .product(toProductResponse(dto.getProduct()))
                .build();
    }
}
