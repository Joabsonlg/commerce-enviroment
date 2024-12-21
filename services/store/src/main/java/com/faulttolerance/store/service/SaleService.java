package com.faulttolerance.store.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class SaleService {
    private final ProductService productService;
    private final AtomicLong orderIdGenerator = new AtomicLong(0);

    public SaleService(ProductService productService) {
        this.productService = productService;
    }

    public Long processSale(Long productId) {
        if (Math.random() < 0.1) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Fail (Error, 0.1, 5s)");
        }

        return orderIdGenerator.incrementAndGet();
    }
}