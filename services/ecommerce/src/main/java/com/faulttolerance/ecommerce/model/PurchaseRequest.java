package com.faulttolerance.ecommerce.model;

public record PurchaseRequest(
    Long productId,
    Integer quantity,
    String currency
) {}
