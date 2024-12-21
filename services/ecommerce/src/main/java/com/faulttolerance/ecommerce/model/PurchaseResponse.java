package com.faulttolerance.ecommerce.model;

import java.math.BigDecimal;

public record PurchaseResponse(
    Long orderId,
    Long productId,
    Integer quantity,
    BigDecimal originalPrice,
    BigDecimal convertedPrice,
    Integer bonusPoints,
    String status
) {}
