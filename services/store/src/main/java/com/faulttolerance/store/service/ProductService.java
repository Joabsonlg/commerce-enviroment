package com.faulttolerance.store.service;

import com.faulttolerance.store.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductService {
    private final Map<Long, Product> products = new ConcurrentHashMap<>();

    public ProductService() {
        products.put(1L, new Product(1L, "Laptop", new BigDecimal("999.99")));
        products.put(2L, new Product(2L, "Smartphone", new BigDecimal("499.99")));
        products.put(3L, new Product(3L, "Tablet", new BigDecimal("299.99")));
    }

    public Product getProduct(Long id) {
        if (Math.random() < 0.2) {
            throw new RuntimeException("Fail (Omission, 0.2, 0s)");
        }
        return products.get(id);
    }
}
