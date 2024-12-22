package com.faulttolerance.store.controller;

import com.faulttolerance.store.model.Product;
import com.faulttolerance.store.service.ProductService;
import com.faulttolerance.store.service.SaleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

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

    @PostMapping("/sell")
    public ResponseEntity<Long> processSale(@RequestParam Long product) {
        try {
            return ResponseEntity.ok(saleService.processSale(product));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).build();
        }
    }
}
