package com.faulttolerance.ecommerce.service;

import com.faulttolerance.ecommerce.model.PurchaseRequest;
import com.faulttolerance.ecommerce.model.PurchaseResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
public class PurchaseService {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    private final RestTemplate restTemplate;

    @Value("${store.url}")
    private String storeUrl;

    @Value("${exchange.url}")
    private String exchangeUrl;

    @Value("${fidelity.url}")
    private String fidelityUrl;

    public PurchaseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "purchase", fallbackMethod = "fallbackPurchase")
    @TimeLimiter(name = "purchase")
    public CompletableFuture<PurchaseResponse> processPurchase(PurchaseRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Processing purchase for product: {}", request.productId());

            // 1. Get product details
            var product = restTemplate.getForObject(
                storeUrl + "/product/" + request.productId(),
                ProductResponse.class
            );

            // 2. Get exchange rate
            var exchangeRate = restTemplate.getForObject(
                exchangeUrl + "/exchange?from=" + product.currency() + "&to=" + request.currency(),
                ExchangeResponse.class
            );

            // 3. Process sale
            var saleRequest = new SaleRequest(
                request.productId(),
                request.quantity(),
                product.price(),
                exchangeRate.rate()
            );
            var sale = restTemplate.postForObject(
                storeUrl + "/sell",
                saleRequest,
                SaleResponse.class
            );

            // 4. Process bonus points (optional)
            Integer bonusPoints = 0;
            try {
                var bonus = restTemplate.postForObject(
                    fidelityUrl + "/bonus",
                    sale.orderId(),
                    BonusResponse.class
                );
                bonusPoints = bonus.points();
            } catch (Exception e) {
                logger.warn("Failed to process bonus points, will be processed later", e);
            }

            return new PurchaseResponse(
                sale.orderId(),
                request.productId(),
                request.quantity(),
                product.price(),
                product.currency(),
                sale.finalPrice(),
                request.currency(),
                bonusPoints,
                "COMPLETED"
            );
        });
    }

    private CompletableFuture<PurchaseResponse> fallbackPurchase(PurchaseRequest request, Throwable t) {
        logger.error("Purchase failed, using fallback", t);
        return CompletableFuture.completedFuture(new PurchaseResponse(
            null,
            request.productId(),
            request.quantity(),
            null,
            null,
            null,
            request.currency(),
            0,
            "FAILED"
        ));
    }

    // Internal record classes for service communication
    private record ProductResponse(Long id, BigDecimal price, String currency) {}
    private record ExchangeResponse(BigDecimal rate) {}
    private record SaleRequest(Long productId, Integer quantity, BigDecimal price, BigDecimal exchangeRate) {}
    private record SaleResponse(Long orderId, BigDecimal finalPrice) {}
    private record BonusResponse(Integer points) {}
}
