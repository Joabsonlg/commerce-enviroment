package com.faulttolerance.exchange.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ExchangeService {
    private final AtomicBoolean crashed = new AtomicBoolean(false);

    public double getExchangeRate() {
        // Se já crashou, continua crashado indefinidamente
        if (crashed.get()) {
            throw new RuntimeException("Service is crashed and not responding");
        }

        // 10% de chance de crash
        if (Math.random() < 0.1 && !crashed.get()) {
            crashed.set(true);
            throw new RuntimeException("Fail (Crash, 0.1, _) - Service crashed and will remain down");
        }

        return ThreadLocalRandom.current().nextDouble(0.1, 10.0);
    }
}
