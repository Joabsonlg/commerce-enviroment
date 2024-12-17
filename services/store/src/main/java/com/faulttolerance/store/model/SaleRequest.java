package com.faulttolerance.store.model;

import java.math.BigDecimal;

public record SaleRequest (Long productId, Integer quantity, BigDecimal exchangeRate) {}
