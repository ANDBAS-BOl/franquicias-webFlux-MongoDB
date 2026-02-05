package com.seti.franchises.application.service;

import com.seti.franchises.domain.entity.Branch;
import com.seti.franchises.domain.entity.Franchise;
import com.seti.franchises.domain.entity.Product;
import com.seti.franchises.domain.port.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service (use cases) for franchise operations.
 * Implements business logic using reactive operators: map, flatMap, switchIfEmpty, zip, onErrorResume.
 * Aligns with PruebaNequi: "Encadene la respuesta entre diferentes flujos usando operadores map, flatMap, switchIfEmpty, merge, zip"
 * and "Use correctamente las señales onNext, onError, onComplete".
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FranchiseUseCaseService {

    private final FranchiseRepository franchiseRepository;

    /**
     * Add a new franchise (POST agregar franquicia).
     */
    public Mono<Franchise> addFranchise(String name) {
        return Mono.justOrEmpty(name)
                .filter(n -> n != null && !n.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre de la franquicia es obligatorio")))
                .map(n -> Franchise.builder()
                        .name(n.trim())
                        .branches(List.of())
                        .build())
                .flatMap(franchiseRepository::save)
                .doOnNext(f -> log.info("Franquicia creada: id={}, name={}", f.getId(), f.getName()))
                .onErrorResume(IllegalArgumentException.class, e -> Mono.error(e));
    }

    /**
     * Add a branch to a franchise (POST agregar sucursal a una franquicia).
     */
    public Mono<Branch> addBranchToFranchise(String franchiseId, String branchName) {
        return Mono.justOrEmpty(franchiseId)
                .filter(id -> id != null && !id.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El id de la franquicia es obligatorio")))
                .flatMap(franchiseRepository::findById)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franchiseId)))
                .zipWith(Mono.justOrEmpty(branchName)
                        .filter(n -> n != null && !n.isBlank())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre de la sucursal es obligatorio"))))
                .map(tuple -> {
                    Franchise f = tuple.getT1();
                    String name = tuple.getT2().trim();
                    Branch newBranch = Branch.builder()
                            .id(UUID.randomUUID().toString())
                            .name(name)
                            .products(List.of())
                            .build();
                    List<Branch> updated = Optional.ofNullable(f.getBranches()).orElse(List.of()).stream()
                            .collect(Collectors.toList());
                    updated.add(newBranch);
                    return Franchise.builder()
                            .id(f.getId())
                            .name(f.getName())
                            .branches(updated)
                            .build();
                })
                .flatMap(franchiseRepository::save)
                .map(saved -> saved.getBranches().stream()
                        .filter(b -> b.getName().equals(branchName.trim()))
                        .findFirst()
                        .orElseThrow())
                .doOnNext(b -> log.info("Sucursal agregada: franchiseId={}, branchId={}", franchiseId, b.getId()))
                .onErrorResume(NotFoundException.class, e -> Mono.error(e))
                .onErrorResume(IllegalArgumentException.class, e -> Mono.error(e));
    }

    /**
     * Add a product to a branch (POST agregar producto a una sucursal).
     */
    public Mono<Product> addProductToBranch(String franchiseId, String branchId, String productName, Integer stockQuantity) {
        if (branchId == null || branchId.isBlank()) {
            return Mono.error(new IllegalArgumentException("El id de la sucursal es obligatorio"));
        }
        Integer stock = Optional.ofNullable(stockQuantity).filter(q -> q >= 0).orElse(0);
        String pname = Optional.ofNullable(productName).map(String::trim).filter(n -> !n.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("El nombre del producto es obligatorio"));
        return Mono.justOrEmpty(franchiseId)
                .flatMap(franchiseRepository::findById)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franchiseId)))
                .map(f -> {
                    Optional<Branch> branchOpt = f.getBranches().stream().filter(b -> branchId.equals(b.getId())).findFirst();
                    if (branchOpt.isEmpty()) {
                        throw new NotFoundException("Sucursal no encontrada: " + branchId);
                    }
                    Branch branch = branchOpt.get();
                    Product newProduct = Product.builder()
                            .id(UUID.randomUUID().toString())
                            .name(pname)
                            .stockQuantity(stock)
                            .build();
                    List<Product> updatedProducts = Optional.ofNullable(branch.getProducts()).orElse(List.of()).stream()
                            .collect(Collectors.toList());
                    updatedProducts.add(newProduct);
                    List<Branch> updatedBranches = f.getBranches().stream()
                            .map(b -> b.getId().equals(branchId)
                                    ? Branch.builder().id(b.getId()).name(b.getName()).products(updatedProducts).build()
                                    : b)
                            .collect(Collectors.toList());
                    Franchise toSave = Franchise.builder()
                            .id(f.getId())
                            .name(f.getName())
                            .branches(updatedBranches)
                            .build();
                    return Tuples.of(toSave, newProduct);
                })
                .flatMap(pair -> franchiseRepository.save(pair.getT1()).thenReturn(pair.getT2()))
                .doOnNext(p -> log.info("Producto agregado: franchiseId={}, branchId={}, productId={}", franchiseId, branchId, p.getId()))
                .onErrorResume(NotFoundException.class, e -> Mono.error(e))
                .onErrorResume(IllegalArgumentException.class, e -> Mono.error(e));
    }

    /**
     * Delete a product from a branch (DELETE eliminar producto de una sucursal).
     */
    public Mono<Void> deleteProductFromBranch(String franchiseId, String branchId, String productId) {
        return Mono.justOrEmpty(franchiseId)
                .flatMap(franchiseRepository::findById)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franchiseId)))
                .map(f -> {
                    Branch branch = f.getBranches().stream().filter(b -> branchId.equals(b.getId())).findFirst().orElse(null);
                    if (branch == null) {
                        throw new NotFoundException("Sucursal no encontrada: " + branchId);
                    }
                    boolean removed = branch.getProducts().removeIf(p -> productId.equals(p.getId()));
                    if (!removed) {
                        throw new NotFoundException("Producto no encontrado: " + productId);
                    }
                    List<Branch> updated = f.getBranches().stream()
                            .map(b -> b.getId().equals(branchId) ? branch : b)
                            .collect(Collectors.toList());
                    return Franchise.builder()
                            .id(f.getId())
                            .name(f.getName())
                            .branches(updated)
                            .build();
                })
                .flatMap(franchiseRepository::save)
                .then()
                .doOnSuccess(v -> log.info("Producto eliminado: franchiseId={}, branchId={}, productId={}", franchiseId, branchId, productId))
                .onErrorResume(NotFoundException.class, e -> Mono.error(e));
    }

    /**
     * Update product stock (PATCH/PUT modificar stock de un producto).
     */
    public Mono<Product> updateProductStock(String franchiseId, String branchId, String productId, Integer newStock) {
        return Mono.justOrEmpty(newStock)
                .filter(q -> q != null && q >= 0)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El stock debe ser un número mayor o igual a 0")))
                .flatMap(stock -> Mono.justOrEmpty(franchiseId)
                        .flatMap(franchiseRepository::findById)
                        .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franchiseId)))
                        .map(f -> {
                            Branch branch = f.getBranches().stream().filter(b -> branchId.equals(b.getId())).findFirst().orElse(null);
                            if (branch == null) {
                                throw new NotFoundException("Sucursal no encontrada: " + branchId);
                            }
                            Product product = branch.getProducts().stream().filter(p -> productId.equals(p.getId())).findFirst().orElse(null);
                            if (product == null) {
                                throw new NotFoundException("Producto no encontrado: " + productId);
                            }
                            Product updatedProduct = Product.builder()
                                    .id(product.getId())
                                    .name(product.getName())
                                    .stockQuantity(stock)
                                    .build();
                            List<Product> updatedProducts = branch.getProducts().stream()
                                    .map(p -> p.getId().equals(productId) ? updatedProduct : p)
                                    .collect(Collectors.toList());
                            List<Branch> updatedBranches = f.getBranches().stream()
                                    .map(b -> b.getId().equals(branchId)
                                            ? Branch.builder().id(branch.getId()).name(branch.getName()).products(updatedProducts).build()
                                            : b)
                                    .collect(Collectors.toList());
                            return Franchise.builder()
                                    .id(f.getId())
                                    .name(f.getName())
                                    .branches(updatedBranches)
                                    .build();
                        })
                        .flatMap(franchiseRepository::save)
                        .flatMap(saved -> {
                            Branch b = saved.getBranches().stream().filter(x -> x.getId().equals(branchId)).findFirst().orElse(null);
                            if (b == null) return Mono.<Product>empty();
                            return Mono.justOrEmpty(b.getProducts().stream().filter(p -> p.getId().equals(productId)).findFirst().orElse(null));
                        }))
                .doOnNext(p -> log.info("Stock actualizado: productId={}, newStock={}", productId, newStock))
                .onErrorResume(NotFoundException.class, e -> Mono.error(e))
                .onErrorResume(IllegalArgumentException.class, e -> Mono.error(e));
    }

    /**
     * Get product with most stock per branch for a franchise (GET producto con más stock por sucursal).
     * Returns a list of entries: branch + product with max stock in that branch.
     */
    public Flux<ProductWithBranchDto> getProductWithMostStockPerBranch(String franchiseId) {
        return Mono.justOrEmpty(franchiseId)
                .flatMap(franchiseRepository::findById)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franchiseId)))
                .flatMapMany(franchise -> Flux.fromIterable(franchise.getBranches())
                        .map(branch -> {
                            Product maxProduct = branch.getProducts().stream()
                                    .max(Comparator.comparingInt(Product::getStockQuantity))
                                    .orElse(null);
                            return new ProductWithBranchDto(branch.getId(), branch.getName(), maxProduct);
                        })
                        .filter(dto -> dto.getProduct() != null))
                .doOnComplete(() -> log.debug("Consulta producto con más stock por sucursal: franchiseId={}", franchiseId))
                .onErrorResume(NotFoundException.class, e -> Mono.error(e));
    }

    /**
     * Update franchise name (punto extra).
     */
    public Mono<Franchise> updateFranchiseName(String franchiseId, String newName) {
        return Mono.justOrEmpty(newName)
                .filter(n -> n != null && !n.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre de la franquicia es obligatorio")))
                .flatMap(name -> Mono.justOrEmpty(franchiseId)
                        .flatMap(franchiseRepository::findById)
                        .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franchiseId)))
                        .map(f -> Franchise.builder()
                                .id(f.getId())
                                .name(name.trim())
                                .branches(f.getBranches())
                                .build())
                        .flatMap(franchiseRepository::save))
                .doOnNext(f -> log.info("Nombre de franquicia actualizado: id={}, name={}", f.getId(), f.getName()))
                .onErrorResume(NotFoundException.class, e -> Mono.error(e))
                .onErrorResume(IllegalArgumentException.class, e -> Mono.error(e));
    }

    /**
     * Update branch name (punto extra).
     */
    public Mono<Branch> updateBranchName(String franchiseId, String branchId, String newName) {
        return Mono.justOrEmpty(newName)
                .filter(n -> n != null && !n.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre de la sucursal es obligatorio")))
                .flatMap(name -> Mono.justOrEmpty(franchiseId)
                        .flatMap(franchiseRepository::findById)
                        .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franchiseId)))
                        .map(f -> {
                            Branch branch = f.getBranches().stream().filter(b -> branchId.equals(b.getId())).findFirst().orElse(null);
                            if (branch == null) {
                                throw new NotFoundException("Sucursal no encontrada: " + branchId);
                            }
                            Branch updated = Branch.builder()
                                    .id(branch.getId())
                                    .name(name.trim())
                                    .products(branch.getProducts())
                                    .build();
                            List<Branch> updatedBranches = f.getBranches().stream()
                                    .map(b -> b.getId().equals(branchId) ? updated : b)
                                    .collect(Collectors.toList());
                            return Franchise.builder()
                                    .id(f.getId())
                                    .name(f.getName())
                                    .branches(updatedBranches)
                                    .build();
                        })
                        .flatMap(franchiseRepository::save)
                        .flatMap(saved -> Mono.justOrEmpty(saved.getBranches().stream()
                                .filter(b -> b.getId().equals(branchId))
                                .findFirst().orElse(null))))
                .doOnNext(b -> log.info("Nombre de sucursal actualizado: branchId={}, name={}", b.getId(), b.getName()))
                .onErrorResume(NotFoundException.class, e -> Mono.error(e))
                .onErrorResume(IllegalArgumentException.class, e -> Mono.error(e));
    }

    /**
     * Update product name (punto extra).
     */
    public Mono<Product> updateProductName(String franchiseId, String branchId, String productId, String newName) {
        return Mono.justOrEmpty(newName)
                .filter(n -> n != null && !n.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre del producto es obligatorio")))
                .flatMap(name -> Mono.justOrEmpty(franchiseId)
                        .flatMap(franchiseRepository::findById)
                        .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franchiseId)))
                        .map(f -> {
                            Branch branch = f.getBranches().stream().filter(b -> branchId.equals(b.getId())).findFirst().orElse(null);
                            if (branch == null) {
                                throw new NotFoundException("Sucursal no encontrada: " + branchId);
                            }
                            Product product = branch.getProducts().stream().filter(p -> productId.equals(p.getId())).findFirst().orElse(null);
                            if (product == null) {
                                throw new NotFoundException("Producto no encontrado: " + productId);
                            }
                            Product updatedProduct = Product.builder()
                                    .id(product.getId())
                                    .name(name.trim())
                                    .stockQuantity(product.getStockQuantity())
                                    .build();
                            List<Product> updatedProducts = branch.getProducts().stream()
                                    .map(p -> p.getId().equals(productId) ? updatedProduct : p)
                                    .collect(Collectors.toList());
                            List<Branch> updatedBranches = f.getBranches().stream()
                                    .map(b -> b.getId().equals(branchId)
                                            ? Branch.builder().id(branch.getId()).name(branch.getName()).products(updatedProducts).build()
                                            : b)
                                    .collect(Collectors.toList());
                            return Franchise.builder()
                                    .id(f.getId())
                                    .name(f.getName())
                                    .branches(updatedBranches)
                                    .build();
                        })
                        .flatMap(franchiseRepository::save)
                        .flatMap(saved -> {
                            Branch b = saved.getBranches().stream().filter(x -> x.getId().equals(branchId)).findFirst().orElse(null);
                            if (b == null) return Mono.<Product>empty();
                            return Mono.justOrEmpty(b.getProducts().stream().filter(p -> p.getId().equals(productId)).findFirst().orElse(null));
                        }))
                .doOnNext(p -> log.info("Nombre de producto actualizado: productId={}, name={}", p.getId(), p.getName()))
                .onErrorResume(NotFoundException.class, e -> Mono.error(e))
                .onErrorResume(IllegalArgumentException.class, e -> Mono.error(e));
    }

    public Mono<Franchise> findById(String id) {
        return franchiseRepository.findById(id);
    }

    public Flux<Franchise> findAll() {
        return franchiseRepository.findAll();
    }

    /**
     * DTO for "product with most stock per branch" response (sucursal + producto).
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @lombok.Builder
    public static class ProductWithBranchDto {
        private String branchId;
        private String branchName;
        private Product product;
    }

    /**
     * Domain exception for 404 (recurso no encontrado).
     */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
