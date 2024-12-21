package com.faulttolerance.exchange.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;


@Service
public class ExchangeService {

    public double getExchangeRate() {
        if (Math.random() < 0.1) {
            throw new RuntimeException("Fail (Crash, 0.1, _) - Service crashed");
        }

        return ThreadLocalRandom.current().nextDouble(0.1, 10.0);
    }
}
