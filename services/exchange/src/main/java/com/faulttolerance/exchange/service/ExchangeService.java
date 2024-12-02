package com.faulttolerance.exchange.service;

import com.faulttolerance.exchange.model.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExchangeService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeService.class);
    private final Map<String, BigDecimal> rates = new ConcurrentHashMap<>();

    public ExchangeService() {
        // Sample exchange rates for testing
        rates.put("USD-EUR", new BigDecimal("0.85"));
        rates.put("USD-GBP", new BigDecimal("0.73"));
        rates.put("EUR-USD", new BigDecimal("1.18"));
        rates.put("EUR-GBP", new BigDecimal("0.86"));
        rates.put("GBP-USD", new BigDecimal("1.37"));
        rates.put("GBP-EUR", new BigDecimal("1.16"));
    }

    @Cacheable(value = "exchangeRates", key = "#fromCurrency + '-' + #toCurrency")
    public ExchangeRate getExchangeRate(String fromCurrency, String toCurrency) {
        // Simulate random failures (20% chance)
        if (Math.random() < 0.2) {
            logger.error("Simulated failure in exchange rate service");
            throw new RuntimeException("Exchange service temporarily unavailable");
        }

        // Simulate latency
        try {
            Thread.sleep((long) (Math.random() * 800));  // Random delay up to 800ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String key = fromCurrency + "-" + toCurrency;
        BigDecimal rate = rates.get(key);

        if (rate == null) {
            if (fromCurrency.equals(toCurrency)) {
                rate = BigDecimal.ONE;
            } else {
                logger.error("Exchange rate not found for {}", key);
                throw new IllegalArgumentException("Exchange rate not found");
            }
        }

        return new ExchangeRate(fromCurrency, toCurrency, rate, LocalDateTime.now());
    }
}
