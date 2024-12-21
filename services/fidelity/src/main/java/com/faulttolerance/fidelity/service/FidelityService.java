package com.faulttolerance.fidelity.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class FidelityService {

    private static final double FAILURE_PROBABILITY = 0.1;
    private static final long FAILURE_DURATION_SECONDS = 30L;
    private static final long FAILURE_DELAY_MILLIS = 2000L;

    private final AtomicReference<LocalDateTime> failureStart = new AtomicReference<>(null);

    public void handleBonus(Long userId, Integer bonus) {
        induceFailureIfNeeded();
    }

    private void induceFailureIfNeeded() {
        LocalDateTime start = failureStart.get();
        LocalDateTime now = LocalDateTime.now();

        if (start != null) {
            if (now.isBefore(start.plusSeconds(FAILURE_DURATION_SECONDS))) {
                simulateTimeDelay();
                throw new RuntimeException("Fail (Time=2s, 0.1, 30s) - Service in failure state");
            } else {
                failureStart.set(null);
            }
        }

        if (Math.random() < FAILURE_PROBABILITY) {
            failureStart.set(LocalDateTime.now());
            simulateTimeDelay();
            throw new RuntimeException("Fail (Time=2s, 0.1, 30s) - Entering failure state");
        }
    }

    private void simulateTimeDelay() {
        try {
            Thread.sleep(FAILURE_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
