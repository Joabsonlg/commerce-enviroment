package com.faulttolerance.fidelity.model;

import java.time.LocalDateTime;

public record BonusPoints(
    Long userId,
    Long orderId,
    Integer points,
    LocalDateTime timestamp,
    String status
) {}
