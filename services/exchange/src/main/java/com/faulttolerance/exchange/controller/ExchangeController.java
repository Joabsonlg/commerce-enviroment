package com.faulttolerance.exchange.controller;

import com.faulttolerance.exchange.model.ExchangeRate;
import com.faulttolerance.exchange.service.ExchangeService;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@Tag(name = "Exchange", description = "Currency exchange operations API")
public class ExchangeController {
    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/rate")
    @Operation(
        summary = "Get exchange rate",
        description = "Get the exchange rate between two currencies with retry mechanism and fallback"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exchange rate retrieved successfully",
            content = @Content(schema = @Schema(implementation = ExchangeRate.class))),
        @ApiResponse(responseCode = "404", description = "Exchange rate not found"),
        @ApiResponse(responseCode = "500", description = "Service temporarily unavailable, fallback rate returned")
    })
    @Timed(value = "exchange.rate", description = "Time taken to get exchange rate")
    @Retry(name = "exchangeRetry", fallbackMethod = "getFallbackRate")
    public ResponseEntity<ExchangeRate> getExchangeRate(
            @Parameter(description = "Source currency code", required = true)
            @RequestParam String from,
            @Parameter(description = "Target currency code", required = true)
            @RequestParam String to) {
        try {
            ExchangeRate rate = exchangeService.getExchangeRate(from, to);
            return ResponseEntity.ok(rate);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(hidden = true)
    private ResponseEntity<ExchangeRate> getFallbackRate(String from, String to, Exception e) {
        return ResponseEntity.ok(new ExchangeRate(
            from,
            to,
            BigDecimal.ONE,
            LocalDateTime.now()
        ));
    }
}
