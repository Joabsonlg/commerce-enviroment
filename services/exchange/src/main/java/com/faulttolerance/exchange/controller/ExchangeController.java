package com.faulttolerance.exchange.controller;

import com.faulttolerance.exchange.service.ExchangeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExchangeController {
    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/exchange")
    public ResponseEntity<Double> getExchangeRate() {
        try {
            double rate = exchangeService.getExchangeRate();
            return ResponseEntity.ok(rate);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).build();
        }
    }
}