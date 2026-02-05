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
        List<BranchResponse> branches = Optional.ofNullable(franchise.getBranches())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toBranchResponse)
                .collect(Collectors.toList());
        return new FranchiseResponse(franchise.getId(), franchise.getName(), branches);
    }

    public BranchResponse toBranchResponse(Branch branch) {
        if (branch == null) return null;
        List<ProductResponse> products = Optional.ofNullable(branch.getProducts())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
        return new BranchResponse(branch.getId(), branch.getName(), products);
    }

    public ProductResponse toProductResponse(Product product) {
        if (product == null) return null;
        boolean enabled = product.getEnabled() != null ? product.getEnabled() : true;
        return new ProductResponse(product.getId(), product.getName(), product.getStockQuantity(), enabled);
    }

    public ProductWithBranchResponse toProductWithBranchResponse(FranchiseUseCaseService.ProductWithBranchDto dto) {
        if (dto == null) return null;
        return new ProductWithBranchResponse(dto.getBranchId(), dto.getBranchName(), toProductResponse(dto.getProduct()));
    }
}
