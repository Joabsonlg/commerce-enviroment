package com.faulttolerance.ecommerce.model;

import java.math.BigDecimal;

public record PurchaseRequest(
    Long productId,
    Integer quantity,
    String currency
) {}
