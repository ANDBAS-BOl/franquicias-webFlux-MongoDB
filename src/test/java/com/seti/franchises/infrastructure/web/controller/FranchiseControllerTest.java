package com.seti.franchises.infrastructure.web.controller;

import com.seti.franchises.application.service.FranchiseUseCaseService;
import com.seti.franchises.domain.entity.Branch;
import com.seti.franchises.domain.entity.Franchise;
import com.seti.franchises.domain.entity.Product;
import com.seti.franchises.infrastructure.web.dto.response.BranchResponse;
import com.seti.franchises.infrastructure.web.dto.response.FranchiseResponse;
import com.seti.franchises.infrastructure.web.dto.response.ProductResponse;
import com.seti.franchises.infrastructure.web.dto.response.ProductWithBranchResponse;
import com.seti.franchises.infrastructure.web.exception.GlobalExceptionHandler;
import com.seti.franchises.infrastructure.web.mapper.ApiMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Pruebas del controlador REST (FranchiseController) con WebTestClient.
 */
@WebFluxTest(FranchiseController.class)
@Import(GlobalExceptionHandler.class)
class FranchiseControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FranchiseUseCaseService useCaseService;

    @MockitoBean
    private ApiMapper apiMapper;

    private static final String FRANCHISE_ID = "f1";
    private static final String BRANCH_ID = "b1";
    private static final String PRODUCT_ID = "p1";

    @Test
    @DisplayName("POST /api/v1/franchises - agregar franquicia retorna 201")
    void addFranchise_returns201() {
        Franchise franchise = Franchise.builder().id(FRANCHISE_ID).name("Franquicia Norte").branches(List.of()).build();
        FranchiseResponse response = new FranchiseResponse(FRANCHISE_ID, "Franquicia Norte", List.of());
        when(useCaseService.addFranchise("Franquicia Norte")).thenReturn(Mono.just(franchise));
        when(apiMapper.toFranchiseResponse(franchise)).thenReturn(response);

        webTestClient.post()
                .uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"Franquicia Norte\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(FRANCHISE_ID)
                .jsonPath("$.name").isEqualTo("Franquicia Norte");
    }

    @Test
    @DisplayName("POST /api/v1/franchises - nombre vacío retorna 400")
    void addFranchise_emptyName_returns400() {
        webTestClient.post()
                .uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("POST /api/v1/franchises/{franchiseId}/branches - agregar sucursal retorna 201")
    void addBranch_returns201() {
        Branch branch = Branch.builder().id(BRANCH_ID).name("Sucursal Centro").products(List.of()).build();
        BranchResponse response = new BranchResponse(BRANCH_ID, "Sucursal Centro", List.of());
        when(useCaseService.addBranchToFranchise(FRANCHISE_ID, "Sucursal Centro")).thenReturn(Mono.just(branch));
        when(apiMapper.toBranchResponse(branch)).thenReturn(response);

        webTestClient.post()
                .uri("/api/v1/franchises/{franchiseId}/branches", FRANCHISE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"Sucursal Centro\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(BRANCH_ID)
                .jsonPath("$.name").isEqualTo("Sucursal Centro");
    }

    @Test
    @DisplayName("POST /api/v1/franchises/{franchiseId}/branches - franquicia no encontrada retorna 404")
    void addBranch_franchiseNotFound_returns404() {
        when(useCaseService.addBranchToFranchise(FRANCHISE_ID, "Sucursal")).thenReturn(
                Mono.error(new FranchiseUseCaseService.NotFoundException("Franquicia no encontrada: " + FRANCHISE_ID)));

        webTestClient.post()
                .uri("/api/v1/franchises/{franchiseId}/branches", FRANCHISE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"Sucursal\"}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("POST /api/v1/franchises/{franchiseId}/branches/{branchId}/products - agregar producto retorna 201")
    void addProduct_returns201() {
        Product product = Product.builder().id(PRODUCT_ID).name("Producto A").stockQuantity(10).build();
        ProductResponse response = new ProductResponse(PRODUCT_ID, "Producto A", 10, true);
        when(useCaseService.addProductToBranch(eq(FRANCHISE_ID), eq(BRANCH_ID), eq("Producto A"), eq(10)))
                .thenReturn(Mono.just(product));
        when(apiMapper.toProductResponse(product)).thenReturn(response);

        webTestClient.post()
                .uri("/api/v1/franchises/{franchiseId}/branches/{branchId}/products", FRANCHISE_ID, BRANCH_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"Producto A\", \"stockQuantity\": 10}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(PRODUCT_ID)
                .jsonPath("$.name").isEqualTo("Producto A")
                .jsonPath("$.stockQuantity").isEqualTo(10);
    }

    @Test
    @DisplayName("DELETE /api/v1/franchises/.../products/{productId} - eliminar producto retorna 204")
    void deleteProduct_returns204() {
        when(useCaseService.deleteProductFromBranch(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}",
                        FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("DELETE producto - no encontrado retorna 404")
    void deleteProduct_notFound_returns404() {
        when(useCaseService.deleteProductFromBranch(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                .thenReturn(Mono.error(new FranchiseUseCaseService.NotFoundException("Producto no encontrado: " + PRODUCT_ID)));

        webTestClient.delete()
                .uri("/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}",
                        FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PATCH .../products/{productId}/disable - borrado lógico retorna 200")
    void disableProduct_returns200() {
        Product disabled = Product.builder().id(PRODUCT_ID).name("Producto A").stockQuantity(10).enabled(false).build();
        ProductResponse response = new ProductResponse(PRODUCT_ID, "Producto A", 10, false);
        when(useCaseService.disableProductInBranch(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)).thenReturn(Mono.just(disabled));
        when(apiMapper.toProductResponse(disabled)).thenReturn(response);

        webTestClient.patch()
                .uri("/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/disable",
                        FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(PRODUCT_ID)
                .jsonPath("$.enabled").isEqualTo(false);
    }

    @Test
    @DisplayName("PATCH .../products/{productId}/stock - modificar stock retorna 200")
    void updateProductStock_returns200() {
        Product product = Product.builder().id(PRODUCT_ID).name("Producto A").stockQuantity(20).build();
        ProductResponse response = new ProductResponse(PRODUCT_ID, "Producto A", 20, true);
        when(useCaseService.updateProductStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, 20)).thenReturn(Mono.just(product));
        when(apiMapper.toProductResponse(product)).thenReturn(response);

        webTestClient.patch()
                .uri("/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock",
                        FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stockQuantity\": 20}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.stockQuantity").isEqualTo(20);
    }

    @Test
    @DisplayName("GET .../branches/products/max-stock - producto con más stock por sucursal retorna 200")
    void getProductWithMostStockPerBranch_returns200() {
        Product product = Product.builder().id(PRODUCT_ID).name("Producto A").stockQuantity(15).build();
        FranchiseUseCaseService.ProductWithBranchDto dto = FranchiseUseCaseService.ProductWithBranchDto.builder()
                .branchId(BRANCH_ID)
                .branchName("Sucursal Centro")
                .product(product)
                .build();
        ProductWithBranchResponse response = new ProductWithBranchResponse(BRANCH_ID, "Sucursal Centro",
                new ProductResponse(PRODUCT_ID, "Producto A", 15, true));
        when(useCaseService.getProductWithMostStockPerBranch(FRANCHISE_ID)).thenReturn(Flux.just(dto));
        when(apiMapper.toProductWithBranchResponse(dto)).thenReturn(response);

        webTestClient.get()
                .uri("/api/v1/franchises/{franchiseId}/branches/products/max-stock", FRANCHISE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].branchId").isEqualTo(BRANCH_ID)
                .jsonPath("$[0].branchName").isEqualTo("Sucursal Centro")
                .jsonPath("$[0].product.id").isEqualTo(PRODUCT_ID);
    }

    @Test
    @DisplayName("GET .../max-stock - franquicia no encontrada retorna 404")
    void getProductWithMostStockPerBranch_notFound_returns404() {
        when(useCaseService.getProductWithMostStockPerBranch(FRANCHISE_ID))
                .thenReturn(Flux.error(new FranchiseUseCaseService.NotFoundException("Franquicia no encontrada: " + FRANCHISE_ID)));

        webTestClient.get()
                .uri("/api/v1/franchises/{franchiseId}/branches/products/max-stock", FRANCHISE_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PATCH .../name - actualizar nombre franquicia (punto extra) retorna 200")
    void updateFranchiseName_returns200() {
        Franchise franchise = Franchise.builder().id(FRANCHISE_ID).name("Nuevo Nombre").branches(List.of()).build();
        FranchiseResponse response = new FranchiseResponse(FRANCHISE_ID, "Nuevo Nombre", List.of());
        when(useCaseService.updateFranchiseName(FRANCHISE_ID, "Nuevo Nombre")).thenReturn(Mono.just(franchise));
        when(apiMapper.toFranchiseResponse(franchise)).thenReturn(response);

        webTestClient.patch()
                .uri("/api/v1/franchises/{franchiseId}/name", FRANCHISE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"Nuevo Nombre\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Nuevo Nombre");
    }

    @Test
    @DisplayName("GET /api/v1/franchises - listar franquicias retorna 200")
    void listFranchises_returns200() {
        Franchise f = Franchise.builder().id(FRANCHISE_ID).name("F1").branches(List.of()).build();
        FranchiseResponse r = new FranchiseResponse(FRANCHISE_ID, "F1", List.of());
        when(useCaseService.findAll()).thenReturn(Flux.just(f));
        when(apiMapper.toFranchiseResponse(any(Franchise.class))).thenReturn(r);

        webTestClient.get()
                .uri("/api/v1/franchises")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(FRANCHISE_ID)
                .jsonPath("$[0].name").isEqualTo("F1");
    }

    @Test
    @DisplayName("GET /api/v1/franchises/{franchiseId} - obtener franquicia retorna 200")
    void getFranchise_returns200() {
        Franchise franchise = Franchise.builder().id(FRANCHISE_ID).name("Franquicia").branches(List.of()).build();
        FranchiseResponse response = new FranchiseResponse(FRANCHISE_ID, "Franquicia", List.of());
        when(useCaseService.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(apiMapper.toFranchiseResponse(franchise)).thenReturn(response);

        webTestClient.get()
                .uri("/api/v1/franchises/{franchiseId}", FRANCHISE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(FRANCHISE_ID)
                .jsonPath("$.name").isEqualTo("Franquicia");
    }

    @Test
    @DisplayName("GET /api/v1/franchises/{franchiseId} - no encontrada retorna 404")
    void getFranchise_notFound_returns404() {
        when(useCaseService.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/franchises/{franchiseId}", FRANCHISE_ID)
                .exchange()
                .expectStatus().isNotFound();
    }
}
