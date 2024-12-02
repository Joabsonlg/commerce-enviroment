package com.faulttolerance.exchange.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRate(
    String fromCurrency,
    String toCurrency,
    BigDecimal rate,
    LocalDateTime timestamp
) {}
