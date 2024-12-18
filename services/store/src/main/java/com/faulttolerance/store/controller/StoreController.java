package com.faulttolerance.store.controller;

import com.faulttolerance.store.model.Product;
import com.faulttolerance.store.model.Sale;
import com.faulttolerance.store.model.SaleRequest;
import com.faulttolerance.store.service.ProductService;
import com.faulttolerance.store.service.SaleService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Store", description = "Store operations API")
public class StoreController {
    private final ProductService productService;
    private final SaleService saleService;

    public StoreController(ProductService productService, SaleService saleService) {
        this.productService = productService;
        this.saleService = saleService;
    }

    @GetMapping("/product/{id}")
    @Operation(summary = "Get product details", description = "Retrieves product information by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found",
            content = @Content(schema = @Schema(implementation = Product.class))),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @Timed(value = "product.get", description = "Time taken to get product details")
    public ResponseEntity<Product> getProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id) {
        Product product = productService.getProduct(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping("/sell")
    @Operation(summary = "Process a sale", description = "Process a sale with the given product, quantity, and exchange rate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sale processed successfully",
            content = @Content(schema = @Schema(implementation = Sale.class))),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request or insufficient stock")
    })
    @Timed(value = "sale.process", description = "Time taken to process sale")
    public ResponseEntity<Sale> processSale(
            @RequestBody SaleRequest saleRequest) {
        try {
            Sale sale = saleService.processSale(saleRequest.productId(), saleRequest.quantity(), saleRequest.exchangeRate());
            return ResponseEntity.ok(sale);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
