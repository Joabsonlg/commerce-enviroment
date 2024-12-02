package com.faulttolerance.store.service;

import com.faulttolerance.store.model.Product;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductService {
    private final Map<Long, Product> products = new ConcurrentHashMap<>();

    public ProductService() {
        // Sample products for testing
        products.put(1L, new Product(1L, "Laptop", new BigDecimal("999.99"), "USD", 10));
        products.put(2L, new Product(2L, "Smartphone", new BigDecimal("499.99"), "USD", 20));
        products.put(3L, new Product(3L, "Tablet", new BigDecimal("299.99"), "USD", 15));
    }

    @Cacheable(value = "products", key = "#id")
    public Product getProduct(Long id) {
        // Simulate database access delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return products.get(id);
    }

    public void updateStock(Long productId, Integer quantity) {
        products.computeIfPresent(productId, (id, product) -> 
            new Product(
                product.id(),
                product.name(),
                product.price(),
                product.currency(),
                product.stock() - quantity
            )
        );
    }
}
