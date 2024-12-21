package com.faulttolerance.store.controller;

import com.faulttolerance.store.model.Product;
import com.faulttolerance.store.service.ProductService;
import com.faulttolerance.store.service.SaleService;
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
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        try {
            Product product = productService.getProduct(id);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/sell/{id}")
    public ResponseEntity<Long> processSale(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(saleService.processSale(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).build();
        }
    }
}
