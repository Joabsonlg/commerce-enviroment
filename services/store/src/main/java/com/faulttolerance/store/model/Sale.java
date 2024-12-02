package com.faulttolerance.store.model;

import java.math.BigDecimal;

public record Sale(
    Long orderId,
    Long productId,
    Integer quantity,
    BigDecimal originalPrice,
    BigDecimal exchangeRate,
    BigDecimal finalPrice
) {}
