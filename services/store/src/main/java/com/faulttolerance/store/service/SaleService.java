package com.faulttolerance.store.service;

import com.faulttolerance.store.model.Product;
import com.faulttolerance.store.model.Sale;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SaleService {
    private final ProductService productService;
    private final AtomicLong orderIdGenerator = new AtomicLong(0);

    public SaleService(ProductService productService) {
        this.productService = productService;
    }

    public Sale processSale(Long productId, Integer quantity, BigDecimal exchangeRate) {
        Product product = productService.getProduct(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        if (product.stock() < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }

        BigDecimal finalPrice = product.price().multiply(BigDecimal.valueOf(quantity)).multiply(exchangeRate);
        Long orderId = orderIdGenerator.incrementAndGet();

        productService.updateStock(productId, quantity);

        return new Sale(
            orderId,
            productId,
            quantity,
            product.price(),
            exchangeRate,
            finalPrice
        );
    }
}
