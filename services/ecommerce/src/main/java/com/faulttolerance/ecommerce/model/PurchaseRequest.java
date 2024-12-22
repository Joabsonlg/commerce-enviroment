package com.faulttolerance.ecommerce.model;

public record PurchaseRequest(
    Long userId,     // id do usuário
    Long productId,  // id do produto
    boolean ft      // flag de tolerância a falhas
) {}
