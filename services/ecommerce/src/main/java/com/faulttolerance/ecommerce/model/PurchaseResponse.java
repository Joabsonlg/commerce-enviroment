package com.faulttolerance.ecommerce.model;

import java.math.BigDecimal;

public record PurchaseResponse(
    Long orderId,
    Long productId,
    Integer quantity,
    BigDecimal originalPrice,
    String originalCurrency,
    BigDecimal convertedPrice,
    String targetCurrency,
    Integer bonusPoints,
    String status
) {}
