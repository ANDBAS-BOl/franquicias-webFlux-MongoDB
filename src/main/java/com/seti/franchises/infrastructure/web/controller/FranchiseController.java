package com.seti.franchises.infrastructure.web.controller;

import com.seti.franchises.application.service.FranchiseUseCaseService;
import com.seti.franchises.infrastructure.web.dto.request.AddBranchRequest;
import com.seti.franchises.infrastructure.web.dto.request.AddFranchiseRequest;
import com.seti.franchises.infrastructure.web.dto.request.AddProductRequest;
import com.seti.franchises.infrastructure.web.dto.request.UpdateNameRequest;
import com.seti.franchises.infrastructure.web.dto.request.UpdateStockRequest;
import com.seti.franchises.infrastructure.web.dto.response.BranchResponse;
import com.seti.franchises.infrastructure.web.dto.response.FranchiseResponse;
import com.seti.franchises.infrastructure.web.dto.response.ProductResponse;
import com.seti.franchises.infrastructure.web.dto.response.ProductWithBranchResponse;
import com.seti.franchises.infrastructure.web.mapper.ApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para franquicias, sucursales y productos.
 * Requisitos funcionales (PruebaNequiAjustada.pdf):
 * 1. POST agregar franquicia
 * 2. POST agregar sucursal a una franquicia
 * 3. POST agregar producto a una sucursal
 * 4. DELETE eliminar producto de una sucursal
 * 5. PATCH/PUT modificar stock de un producto
 * 6. GET producto con más stock por sucursal para una franquicia
 * Puntos extra: PUT actualizar nombre de franquicia, sucursal y producto.
 */
@Tag(name = "Franquicias", description = "API de franquicias, sucursales y productos")
@RestController
@RequestMapping("/api/v1/franchises")
@RequiredArgsConstructor
public class FranchiseController {

    private final FranchiseUseCaseService useCaseService;
    private final ApiMapper apiMapper;

    @Operation(summary = "Agregar franquicia", description = "Crea una nueva franquicia (nombre + listado de sucursales vacío)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Franquicia creada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FranchiseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FranchiseResponse> addFranchise(@Valid @RequestBody AddFranchiseRequest request) {
        return useCaseService.addFranchise(request.getName())
                .map(apiMapper::toFranchiseResponse);
    }

    @Operation(summary = "Agregar sucursal", description = "Agrega una sucursal a una franquicia existente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sucursal creada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BranchResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Franquicia no encontrada")
    })
    @PostMapping(value = "/{franchiseId}/branches", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BranchResponse> addBranch(
            @Parameter(description = "ID de la franquicia") @PathVariable String franchiseId,
            @Valid @RequestBody AddBranchRequest request) {
        return useCaseService.addBranchToFranchise(franchiseId, request.getName())
                .map(apiMapper::toBranchResponse);
    }

    @Operation(summary = "Agregar producto", description = "Agrega un producto a una sucursal de una franquicia")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Franquicia o sucursal no encontrada")
    })
    @PostMapping(value = "/{franchiseId}/branches/{branchId}/products", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductResponse> addProduct(
            @Parameter(description = "ID de la franquicia") @PathVariable String franchiseId,
            @Parameter(description = "ID de la sucursal") @PathVariable String branchId,
            @Valid @RequestBody AddProductRequest request) {
        Integer stock = request.getStockQuantity() != null ? request.getStockQuantity() : 0;
        return useCaseService.addProductToBranch(franchiseId, branchId, request.getName(), stock)
                .map(apiMapper::toProductResponse);
    }

    @Operation(summary = "Eliminar producto", description = "Elimina un producto de una sucursal")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(responseCode = "404", description = "Franquicia, sucursal o producto no encontrado")
    })
    @DeleteMapping("/{franchiseId}/branches/{branchId}/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteProduct(
            @Parameter(description = "ID de la franquicia") @PathVariable String franchiseId,
            @Parameter(description = "ID de la sucursal") @PathVariable String branchId,
            @Parameter(description = "ID del producto") @PathVariable String productId) {
        return useCaseService.deleteProductFromBranch(franchiseId, branchId, productId);
    }

    @Operation(summary = "Modificar stock", description = "Actualiza la cantidad en stock de un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock actualizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Franquicia, sucursal o producto no encontrado")
    })
    @PatchMapping(value = "/{franchiseId}/branches/{branchId}/products/{productId}/stock", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ProductResponse> updateProductStock(
            @Parameter(description = "ID de la franquicia") @PathVariable String franchiseId,
            @Parameter(description = "ID de la sucursal") @PathVariable String branchId,
            @Parameter(description = "ID del producto") @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest request) {
        return useCaseService.updateProductStock(franchiseId, branchId, productId, request.getStockQuantity())
                .map(apiMapper::toProductResponse);
    }

    @Operation(summary = "Producto con más stock por sucursal", description = "Lista el producto con mayor stock en cada sucursal de la franquicia (indica a qué sucursal pertenece)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de productos con más stock por sucursal",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductWithBranchResponse.class))),
            @ApiResponse(responseCode = "404", description = "Franquicia no encontrada")
    })
    @GetMapping(value = "/{franchiseId}/branches/products/max-stock", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ProductWithBranchResponse> getProductWithMostStockPerBranch(
            @Parameter(description = "ID de la franquicia") @PathVariable String franchiseId) {
        return useCaseService.getProductWithMostStockPerBranch(franchiseId)
                .map(apiMapper::toProductWithBranchResponse);
    }

    // --- Puntos extra: actualizar nombres ---

    @Operation(summary = "Actualizar nombre de franquicia", description = "Modifica el nombre de una franquicia (punto extra)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nombre actualizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FranchiseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Franquicia no encontrada")
    })
    @PatchMapping(value = "/{franchiseId}/name", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<FranchiseResponse> updateFranchiseName(
            @Parameter(description = "ID de la franquicia") @PathVariable String franchiseId,
            @Valid @RequestBody UpdateNameRequest request) {
        return useCaseService.updateFranchiseName(franchiseId, request.getName())
                .map(apiMapper::toFranchiseResponse);
    }

    @Operation(summary = "Actualizar nombre de sucursal", description = "Modifica el nombre de una sucursal (punto extra)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nombre actualizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BranchResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Franquicia o sucursal no encontrada")
    })
    @PatchMapping(value = "/{franchiseId}/branches/{branchId}/name", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BranchResponse> updateBranchName(
            @Parameter(description = "ID de la franquicia") @PathVariable String franchiseId,
            @Parameter(description = "ID de la sucursal") @PathVariable String branchId,
            @Valid @RequestBody UpdateNameRequest request) {
        return useCaseService.updateBranchName(franchiseId, branchId, request.getName())
                .map(apiMapper::toBranchResponse);
    }

    @Operation(summary = "Actualizar nombre de producto", description = "Modifica el nombre de un producto (punto extra)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nombre actualizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Franquicia, sucursal o producto no encontrado")
    })
    @PatchMapping(value = "/{franchiseId}/branches/{branchId}/products/{productId}/name", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ProductResponse> updateProductName(
            @Parameter(description = "ID de la franquicia") @PathVariable String franchiseId,
            @Parameter(description = "ID de la sucursal") @PathVariable String branchId,
            @Parameter(description = "ID del producto") @PathVariable String productId,
            @Valid @RequestBody UpdateNameRequest request) {
        return useCaseService.updateProductName(franchiseId, branchId, productId, request.getName())
                .map(apiMapper::toProductResponse);
    }

    // --- Consultas opcionales ---

    @Operation(summary = "Obtener franquicia por ID", description = "Devuelve una franquicia con sus sucursales y productos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Franquicia encontrada"),
            @ApiResponse(responseCode = "404", description = "Franquicia no encontrada")
    })
    @GetMapping(value = "/{franchiseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<FranchiseResponse> getFranchise(@Parameter(description = "ID de la franquicia") @PathVariable String franchiseId) {
        return useCaseService.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseUseCaseService.NotFoundException("Franquicia no encontrada: " + franchiseId)))
                .map(apiMapper::toFranchiseResponse);
    }

    @Operation(summary = "Listar franquicias", description = "Devuelve todas las franquicias")
    @ApiResponse(responseCode = "200", description = "Listado de franquicias")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<FranchiseResponse> listFranchises() {
        return useCaseService.findAll()
                .map(apiMapper::toFranchiseResponse);
    }
}
