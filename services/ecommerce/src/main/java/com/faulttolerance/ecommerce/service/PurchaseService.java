package com.faulttolerance.ecommerce.service;

import com.faulttolerance.ecommerce.model.PurchaseRequest;
import com.faulttolerance.ecommerce.model.PurchaseResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PurchaseService {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    @Value("${store.url}")
    private String storeUrl;

    @Value("${exchange.url}")
    private String exchangeUrl;

    @Value("${fidelity.url}")
    private String fidelityUrl;

    private final RestTemplate restTemplate;
    private final AtomicReference<Double> lastKnownRate = new AtomicReference<>(1.0);
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final List<FidelityRequest> pendingFidelityRequests = new LinkedList<>();
    private final AtomicReference<LocalDateTime> degradeModeStart = new AtomicReference<>(null);
    private static final long FAILURE_DURATION_SECONDS = 30L;

    public PurchaseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        // Inicia o processador de bônus pendentes
        startPendingBonusProcessor();
    }

    public CompletableFuture<PurchaseResponse> processPurchase(PurchaseRequest request) {
        logger.info("Processing purchase for product: {}, user: {}, ft: {}",
            request.productId(), request.userId(), request.ft());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. Consulta produto (Request 1)
                var product = getProduct(request.productId(), request.ft());

                // 2. Obtém taxa de câmbio (Request 2)
                var exchangeRate = getExchangeRate(request.ft());

                // 3. Processa venda (Request 3)
                var transactionId = processSale(request.productId(), request.ft());

                // 4. Registra bônus (Request 4)
                int bonus = (int) Math.round(product.value());
                registerBonus(request.userId(), bonus, request.ft());

                return new PurchaseResponse(transactionId);
            } catch (Exception e) {
                logger.error("Failed to process purchase", e);
                throw e;
            }
        }, executorService);
    }

    @CircuitBreaker(name = "storeProduct", fallbackMethod = "fallbackProduct")
    @TimeLimiter(name = "storeProduct")
    private ProductResponse getProduct(Long productId, boolean ft) {
        String url = storeUrl + "/product/" + productId;
        try {
            return restTemplate.getForObject(url, ProductResponse.class);
        } catch (Exception e) {
            if (ft) {
                return fallbackProduct(productId, e, ft);
            }
            throw e;
        }
    }

    private ProductResponse fallbackProduct(Long productId, Throwable t, boolean ft) {
        logger.warn("Product service failed, using fallback. Error: {}", t.getMessage());
        return new ProductResponse(productId, "Fallback Product", 0.0);
    }

    @CircuitBreaker(name = "exchangeRate", fallbackMethod = "fallbackExchangeRate")
    @TimeLimiter(name = "exchangeRate")
    private double getExchangeRate(boolean ft) {
        try {
            Double rate = restTemplate.getForObject(exchangeUrl + "/exchange", Double.class);
            if (rate != null && rate > 0) {
                lastKnownRate.set(rate);
                return rate;
            }
            throw new RuntimeException("Invalid exchange rate");
        } catch (Exception e) {
            if (ft) {
                return fallbackExchangeRate(e, ft);
            }
            throw e;
        }
    }

    private double fallbackExchangeRate(Throwable t, boolean ft) {
        logger.warn("Exchange service failed, using last known rate: {}", lastKnownRate.get());
        return lastKnownRate.get();
    }

    @CircuitBreaker(name = "storeSale", fallbackMethod = "fallbackSale")
    @TimeLimiter(name = "storeSale")
    private String processSale(Long productId, boolean ft) {
        String url = storeUrl + "/sell?product=" + productId;
        try {
            var response = restTemplate.postForEntity(url, null, String.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("Invalid response from store service");
        } catch (Exception e) {
            if (ft) {
                return fallbackSale(productId, e, ft);
            }
            throw e;
        }
    }

    private String fallbackSale(Long productId, Throwable t, boolean ft) {
        logger.warn("Store service failed, using fallback transaction ID. Error: {}", t.getMessage());
        return UUID.randomUUID().toString();
    }

    private void registerBonus(Long userId, int bonus, boolean ft) {
        if (!ft) {
            String url = String.format("%s/bonus?user=%s&bonus=%d", fidelityUrl, userId, bonus);
            restTemplate.postForEntity(url, null, Void.class);
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                if (isInDegradeMode()) {
                    logger.warn("Fidelity in degrade mode => storing request for later: user={}, bonus={}", userId, bonus);
                    storeFidelityRequest(userId, bonus);
                    return;
                }

                String url = String.format("%s/bonus?user=%s&bonus=%d", fidelityUrl, userId, bonus);
                restTemplate.postForEntity(url, null, Void.class);
                logger.info("Bonus registered successfully for user: {}, bonus: {}", userId, bonus);
            } catch (Exception e) {
                logger.warn("Failed to register bonus, storing for retry. User: {}, bonus: {}", userId, bonus);
                storeFidelityRequest(userId, bonus);
                activateDegradeMode();
            }
        }, executorService);
    }

    private void startPendingBonusProcessor() {
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    if (!isInDegradeMode() && !pendingFidelityRequests.isEmpty()) {
                        processPendingBonuses();
                    }
                    Thread.sleep(5000); // Verifica a cada 5 segundos
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, executorService);
    }

    private void processPendingBonuses() {
        List<FidelityRequest> successfulRequests = new LinkedList<>();

        for (FidelityRequest request : pendingFidelityRequests) {
            try {
                String url = String.format("%s/bonus?user=%s&bonus=%d",
                    fidelityUrl, request.userId(), request.bonus());

                restTemplate.postForEntity(url, null, Void.class);
                successfulRequests.add(request);
                logger.info("Processed pending bonus: user={}, bonus={}",
                    request.userId(), request.bonus());
            } catch (Exception e) {
                logger.warn("Failed to process pending bonus: user={}, bonus={}",
                    request.userId(), request.bonus());
                activateDegradeMode();
                break;
            }
        }

        pendingFidelityRequests.removeAll(successfulRequests);
    }

    private boolean isInDegradeMode() {
        LocalDateTime start = degradeModeStart.get();
        return start != null && LocalDateTime.now().isBefore(start.plusSeconds(FAILURE_DURATION_SECONDS));
    }

    private void activateDegradeMode() {
        degradeModeStart.set(LocalDateTime.now());
    }

    private void storeFidelityRequest(Long userId, int bonus) {
        pendingFidelityRequests.add(new FidelityRequest(userId, bonus));
        logger.info("Fidelity request stored => user={}, bonus={}. Pending count={}",
            userId, bonus, pendingFidelityRequests.size());
    }

    private record ProductResponse(Long id, String name, Double value) {}
    private record FidelityRequest(Long userId, int bonus) {}
}
