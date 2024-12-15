package com.faulttolerance.exchange.service;

import com.faulttolerance.exchange.model.ExchangeRate;
import com.faulttolerance.exchange.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ExchangeService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeService.class);

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;

        if (exchangeRateRepository.count() == 0) {
            initializeSampleRates();
        }
    }

    private void initializeSampleRates() {
        exchangeRateRepository.save(new ExchangeRate("USD", "EUR", new BigDecimal("0.85"), LocalDateTime.now()));
        exchangeRateRepository.save(new ExchangeRate("USD", "GBP", new BigDecimal("0.73"), LocalDateTime.now()));
        exchangeRateRepository.save(new ExchangeRate("EUR", "USD", new BigDecimal("1.18"), LocalDateTime.now()));
        exchangeRateRepository.save(new ExchangeRate("EUR", "GBP", new BigDecimal("0.86"), LocalDateTime.now()));
        exchangeRateRepository.save(new ExchangeRate("GBP", "USD", new BigDecimal("1.37"), LocalDateTime.now()));
        exchangeRateRepository.save(new ExchangeRate("GBP", "EUR", new BigDecimal("1.16"), LocalDateTime.now()));
        exchangeRateRepository.save(new ExchangeRate("USD", "BRL", new BigDecimal("6.04"), LocalDateTime.now()));
        exchangeRateRepository.save(new ExchangeRate("EUR", "BRL", new BigDecimal("6.29"), LocalDateTime.now()));
        exchangeRateRepository.save(new ExchangeRate("BRL", "USD", new BigDecimal("0.1656"), LocalDateTime.now()));
        exchangeRateRepository.save(new ExchangeRate("BRL", "EUR", new BigDecimal("0.1590"), LocalDateTime.now()));

    }

    @Cacheable(value = "exchangeRates", key = "#fromCurrency + '-' + #toCurrency")
    public ExchangeRate getExchangeRate(String fromCurrency, String toCurrency) {
        if (Math.random() < 0.2) {
            logger.error("Simulated failure in exchange rate service");
            throw new RuntimeException("Exchange service temporarily unavailable");
        }

        try {
            Thread.sleep((long) (Math.random() * 800));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String key = fromCurrency + "-" + toCurrency;
        Optional<ExchangeRate> exchangeRate = exchangeRateRepository.findById(key);

        if (exchangeRate.isPresent()) {
            return exchangeRate.get();
        } else if (fromCurrency.equals(toCurrency)) {
            return new ExchangeRate(fromCurrency, toCurrency, BigDecimal.ONE, LocalDateTime.now());
        } else {
            logger.error("Exchange rate not found for {}", key);
            throw new IllegalArgumentException("Exchange rate not found");
        }
    }
}
