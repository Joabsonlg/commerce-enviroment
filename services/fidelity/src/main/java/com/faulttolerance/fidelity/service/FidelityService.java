package com.faulttolerance.fidelity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class FidelityService {
    private static final Logger logger = LoggerFactory.getLogger(FidelityService.class);
    private static final double FAILURE_PROBABILITY = 0.1;
    private static final long FAILURE_DURATION_SECONDS = 30L;
    private static final long FAILURE_DELAY_MILLIS = 2000L;

    private final AtomicReference<LocalDateTime> failureStart = new AtomicReference<>(null);
    private final Map<Long, Integer> userBonuses = new ConcurrentHashMap<>();

    public void handleBonus(Long userId, Integer bonus) {
        // Primeiro verifica e induz falha se necessário
        induceFailureIfNeeded();

        // Se passou pela verificação de falha, processa o bônus
        processBonus(userId, bonus);
    }

    private void processBonus(Long userId, Integer bonus) {
        // Atualiza o bônus do usuário
        userBonuses.compute(userId, (key, currentBonus) -> 
            currentBonus == null ? bonus : currentBonus + bonus
        );

        logger.info("Bonus processed successfully: user={}, bonus={}, totalBonus={}", 
            userId, bonus, userBonuses.get(userId));
    }

    private void induceFailureIfNeeded() {
        LocalDateTime start = failureStart.get();
        LocalDateTime now = LocalDateTime.now();

        // Se está em estado de falha
        if (start != null) {
            if (now.isBefore(start.plusSeconds(FAILURE_DURATION_SECONDS))) {
                simulateTimeDelay();
                throw new RuntimeException("Fail (Time=2s, 0.1, 30s) - Service in failure state");
            } else {
                // Reseta o estado de falha após 30s
                failureStart.set(null);
                logger.info("Service recovered from failure state");
            }
        }

        // 10% de chance de entrar em estado de falha
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
            throw new RuntimeException("Interrupted while simulating delay", e);
        }
    }

    // Método para consulta (útil para testes)
    public Integer getUserBonus(Long userId) {
        return userBonuses.getOrDefault(userId, 0);
    }
}
