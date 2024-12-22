package com.faulttolerance.store.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.LocalDateTime;

@Service
public class SaleService {
    private final ProductService productService;
    private final AtomicLong orderIdGenerator = new AtomicLong(0);
    private final AtomicBoolean inErrorState = new AtomicBoolean(false);
    private LocalDateTime errorStateUntil;

    public SaleService(ProductService productService) {
        this.productService = productService;
    }

    public Long processSale(Long productId) {
        // Verifica se o produto existe
        var product = productService.getProduct(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        // Se está em estado de erro e ainda não passou o tempo
        if (inErrorState.get() && LocalDateTime.now().isBefore(errorStateUntil)) {
            throw new RuntimeException("Service in error state");
        }

        // 10% de chance de entrar em estado de erro por 5s
        if (Math.random() < 0.1) {
            inErrorState.set(true);
            errorStateUntil = LocalDateTime.now().plusSeconds(5);
            throw new RuntimeException("Fail (Error, 0.1, 5s)");
        }

        // Se chegou aqui, reseta o estado de erro se necessário
        inErrorState.set(false);
        
        // Gera e retorna o ID único da transação
        return orderIdGenerator.incrementAndGet();
    }
}