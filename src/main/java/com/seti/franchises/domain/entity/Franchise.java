package com.seti.franchises.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a franchise (franquicia).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Franchise {

    private String id;
    private String name;

    @Builder.Default
    private List<Branch> branches = new ArrayList<>();
}
