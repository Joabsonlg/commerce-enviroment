package com.faulttolerance.fidelity.model;

public record UserPoints(
    Long userId,
    Integer totalPoints,
    Integer pendingPoints
) {}
