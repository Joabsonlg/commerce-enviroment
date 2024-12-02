package com.faulttolerance.store.model;

import java.math.BigDecimal;

public record Product(
    Long id,
    String name,
    BigDecimal price,
    String currency,
    Integer stock
) {}
