package com.faulttolerance.store.model;

import java.io.Serializable;
import java.math.BigDecimal;

public record Product(
        Long id,
        String name,
        BigDecimal value
) implements Serializable {}
