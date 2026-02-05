package com.seti.franchises.application.service;

import com.seti.franchises.domain.entity.Branch;
import com.seti.franchises.domain.entity.Franchise;
import com.seti.franchises.domain.entity.Product;
import com.seti.franchises.domain.port.FranchiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio de casos de uso (FranchiseUseCaseService).
 * Cumple Etapa 4 del plan: cobertura > 60%, deseable ≥ 80%.
 * Verifica flujos reactivos (map, flatMap, switchIfEmpty, onErrorResume) y manejo de errores.
 * Alineado con PruebaNequi: "incluya pruebas unitarias, asegúrese de obtener una cobertura mayor al 60% deseable 80%".
 */
@ExtendWith(MockitoExtension.class)
class FranchiseUseCaseServiceTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private FranchiseUseCaseService useCaseService;

    private static final String FRANCHISE_ID = "f1";
    private static final String BRANCH_ID = "b1";
    private static final String PRODUCT_ID = "p1";

    private Franchise franchiseWithBranch;
    private Branch branchWithProduct;
    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(PRODUCT_ID).name("Producto A").stockQuantity(10).build();
        // Listas mutables: el servicio hace removeIf sobre products en deleteProductFromBranch
        branchWithProduct = Branch.builder()
                .id(BRANCH_ID)
                .name("Sucursal Centro")
                .products(new ArrayList<>(List.of(product)))
                .build();
        franchiseWithBranch = Franchise.builder()
                .id(FRANCHISE_ID)
                .name("Franquicia Test")
                .branches(new ArrayList<>(List.of(branchWithProduct)))
                .build();
    }

    @Nested
    @DisplayName("addFranchise")
    class AddFranchiseTests {

        @Test
        @DisplayName("crea franquicia cuando el nombre es válido")
        void addFranchise_success() {
            Franchise saved = Franchise.builder().id(FRANCHISE_ID).name("Nueva Franquicia").branches(List.of()).build();
            when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(saved));

            StepVerifier.create(useCaseService.addFranchise("Nueva Franquicia"))
                    .expectNext(saved)
                    .verifyComplete();

            verify(franchiseRepository).save(any(Franchise.class));
        }

        @Test
        @DisplayName("retorna error cuando el nombre está vacío")
        void addFranchise_emptyName() {
            StepVerifier.create(useCaseService.addFranchise("   "))
                    .expectError(IllegalArgumentException.class)
                    .verify();

            StepVerifier.create(useCaseService.addFranchise(null))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("addBranchToFranchise")
    class AddBranchToFranchiseTests {

        @Test
        @DisplayName("agrega sucursal cuando franquicia existe")
        void addBranch_success() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithBranch));
            Franchise savedWithNewBranch = Franchise.builder()
                    .id(FRANCHISE_ID)
                    .name(franchiseWithBranch.getName())
                    .branches(List.of(branchWithProduct, Branch.builder().id("b2").name("Nueva Sucursal").products(List.of()).build()))
                    .build();
            when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(savedWithNewBranch));

            StepVerifier.create(useCaseService.addBranchToFranchise(FRANCHISE_ID, "Nueva Sucursal"))
                    .expectNextMatches(b -> "Nueva Sucursal".equals(b.getName()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("retorna NotFoundException cuando franquicia no existe")
        void addBranch_franchiseNotFound() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(useCaseService.addBranchToFranchise(FRANCHISE_ID, "Sucursal"))
                    .expectError(FranchiseUseCaseService.NotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("retorna error cuando nombre de sucursal está vacío")
        void addBranch_emptyBranchName() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithBranch));

            StepVerifier.create(useCaseService.addBranchToFranchise(FRANCHISE_ID, "   "))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("addProductToBranch")
    class AddProductToBranchTests {

        @Test
        @DisplayName("agrega producto cuando franquicia y sucursal existen")
        void addProduct_success() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithBranch));
            when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(franchiseWithBranch));

            StepVerifier.create(useCaseService.addProductToBranch(FRANCHISE_ID, BRANCH_ID, "Nuevo Producto", 5))
                    .expectNextMatches(p -> "Nuevo Producto".equals(p.getName()) && Integer.valueOf(5).equals(p.getStockQuantity()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("retorna NotFoundException cuando franquicia no existe")
        void addProduct_franchiseNotFound() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(useCaseService.addProductToBranch(FRANCHISE_ID, BRANCH_ID, "Producto", 0))
                    .expectError(FranchiseUseCaseService.NotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("retorna NotFoundException cuando sucursal no existe")
        void addProduct_branchNotFound() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithBranch));

            StepVerifier.create(useCaseService.addProductToBranch(FRANCHISE_ID, "branch-inexistente", "Producto", 0))
                    .expectError(FranchiseUseCaseService.NotFoundException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("deleteProductFromBranch")
    class DeleteProductFromBranchTests {

        @Test
        @DisplayName("elimina producto cuando existe")
        void deleteProduct_success() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithBranch));
            Franchise withoutProduct = Franchise.builder()
                    .id(FRANCHISE_ID)
                    .name(franchiseWithBranch.getName())
                    .branches(List.of(Branch.builder().id(BRANCH_ID).name(branchWithProduct.getName()).products(List.of()).build()))
                    .build();
            when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(withoutProduct));

            StepVerifier.create(useCaseService.deleteProductFromBranch(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("retorna NotFoundException cuando franquicia no existe")
        void deleteProduct_franchiseNotFound() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(useCaseService.deleteProductFromBranch(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                    .expectError(FranchiseUseCaseService.NotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("retorna NotFoundException cuando producto no existe")
        void deleteProduct_productNotFound() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithBranch));

            StepVerifier.create(useCaseService.deleteProductFromBranch(FRANCHISE_ID, BRANCH_ID, "producto-inexistente"))
                    .expectError(FranchiseUseCaseService.NotFoundException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("updateProductStock")
    class UpdateProductStockTests {

        @Test
        @DisplayName("actualiza stock cuando datos son válidos")
        void updateStock_success() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithBranch));
            Product updated = Product.builder().id(PRODUCT_ID).name(product.getName()).stockQuantity(20).build();
            Branch branchUpdated = Branch.builder().id(BRANCH_ID).name(branchWithProduct.getName()).products(List.of(updated)).build();
            Franchise saved = Franchise.builder().id(FRANCHISE_ID).name(franchiseWithBranch.getName()).branches(List.of(branchUpdated)).build();
            when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(saved));

            StepVerifier.create(useCaseService.updateProductStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, 20))
                    .expectNextMatches(p -> Integer.valueOf(20).equals(p.getStockQuantity()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("retorna error cuando stock es negativo")
        void updateStock_negativeStock() {
            StepVerifier.create(useCaseService.updateProductStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, -1))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("retorna NotFoundException cuando franquicia no existe")
        void updateStock_franchiseNotFound() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(useCaseService.updateProductStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, 10))
                    .expectError(FranchiseUseCaseService.NotFoundException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("getProductWithMostStockPerBranch")
    class GetProductWithMostStockPerBranchTests {

        @Test
        @DisplayName("retorna producto con más stock por sucursal")
        void getMaxStock_success() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithBranch));

            StepVerifier.create(useCaseService.getProductWithMostStockPerBranch(FRANCHISE_ID))
                    .expectNextMatches(dto -> dto.getBranchId().equals(BRANCH_ID)
                            && dto.getProduct() != null
                            && dto.getProduct().getId().equals(PRODUCT_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("retorna NotFoundException cuando franquicia no existe")
        void getMaxStock_franchiseNotFound() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(useCaseService.getProductWithMostStockPerBranch(FRANCHISE_ID))
                    .expectError(FranchiseUseCaseService.NotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("no emite sucursales sin productos")
        void getMaxStock_branchesWithoutProducts_filtered() {
            Branch emptyBranch = Branch.builder().id("b-empty").name("Sucursal Vacía").products(List.of()).build();
            Franchise f = Franchise.builder().id(FRANCHISE_ID).name("F").branches(List.of(emptyBranch)).build();
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(f));

            StepVerifier.create(useCaseService.getProductWithMostStockPerBranch(FRANCHISE_ID))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("updateFranchiseName (punto extra)")
    class UpdateFranchiseNameTests {

        @Test
        @DisplayName("actualiza nombre de franquicia")
        void updateFranchiseName_success() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithBranch));
            Franchise updated = Franchise.builder().id(FRANCHISE_ID).name("Nombre Actualizado").branches(franchiseWithBranch.getBranches()).build();
            when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updated));

            StepVerifier.create(useCaseService.updateFranchiseName(FRANCHISE_ID, "Nombre Actualizado"))
                    .expectNextMatches(f -> "Nombre Actualizado".equals(f.getName()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("retorna error cuando nombre está vacío")
        void updateFranchiseName_emptyName() {
            StepVerifier.create(useCaseService.updateFranchiseName(FRANCHISE_ID, "   "))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("updateBranchName (punto extra)")
    class UpdateBranchNameTests {

        @Test
        @DisplayName("actualiza nombre de sucursal")
        void updateBranchName_success() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithBranch));
            Branch updatedBranch = Branch.builder().id(BRANCH_ID).name("Sucursal Renombrada").products(branchWithProduct.getProducts()).build();
            Franchise saved = Franchise.builder().id(FRANCHISE_ID).name(franchiseWithBranch.getName()).branches(List.of(updatedBranch)).build();
            when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(saved));

            StepVerifier.create(useCaseService.updateBranchName(FRANCHISE_ID, BRANCH_ID, "Sucursal Renombrada"))
                    .expectNextMatches(b -> "Sucursal Renombrada".equals(b.getName()))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("updateProductName (punto extra)")
    class UpdateProductNameTests {

        @Test
        @DisplayName("actualiza nombre de producto")
        void updateProductName_success() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithBranch));
            Product updatedProduct = Product.builder().id(PRODUCT_ID).name("Producto Renombrado").stockQuantity(product.getStockQuantity()).build();
            Branch b = Branch.builder().id(BRANCH_ID).name(branchWithProduct.getName()).products(List.of(updatedProduct)).build();
            Franchise saved = Franchise.builder().id(FRANCHISE_ID).name(franchiseWithBranch.getName()).branches(List.of(b)).build();
            when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(saved));

            StepVerifier.create(useCaseService.updateProductName(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "Producto Renombrado"))
                    .expectNextMatches(p -> "Producto Renombrado".equals(p.getName()))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("findById y findAll")
    class FindTests {

        @Test
        @DisplayName("findById delega al repositorio")
        void findById() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithBranch));

            StepVerifier.create(useCaseService.findById(FRANCHISE_ID))
                    .expectNext(franchiseWithBranch)
                    .verifyComplete();
        }

        @Test
        @DisplayName("findAll delega al repositorio")
        void findAll() {
            when(franchiseRepository.findAll()).thenReturn(Flux.just(franchiseWithBranch));

            StepVerifier.create(useCaseService.findAll())
                    .expectNext(franchiseWithBranch)
                    .verifyComplete();
        }
    }
}
