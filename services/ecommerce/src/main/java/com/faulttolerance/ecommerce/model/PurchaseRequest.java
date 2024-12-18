package com.faulttolerance.ecommerce.model;

public record PurchaseRequest(
    Long userId,
    Long productId,
    Integer quantity
) {}
