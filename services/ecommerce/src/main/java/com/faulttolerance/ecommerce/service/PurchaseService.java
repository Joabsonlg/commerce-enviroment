package com.faulttolerance.ecommerce.service;

import com.faulttolerance.ecommerce.model.PurchaseRequest;
import com.faulttolerance.ecommerce.model.PurchaseResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
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

    private volatile BigDecimal lastKnownRate = BigDecimal.ONE;

    private final AtomicReference<LocalDateTime> degradeModeStart = new AtomicReference<>(null);
    private static final long FAILURE_DURATION_SECONDS = 30L;
    private static final long FAILURE_DELAY_MILLIS = 2000L;

    private final List<FidelityRequest> pendingFidelityRequests = new LinkedList<>();

    private final Executor fidelityExecutor = Executors.newFixedThreadPool(10);

    private final PurchaseService self;

    @Autowired
    public PurchaseService(RestTemplate restTemplate, @Lazy PurchaseService self) {
        this.restTemplate = restTemplate;
        this.self = self;
    }

    public CompletableFuture<PurchaseResponse> processPurchase(PurchaseRequest request) {
        logger.info("Processing purchase for product: {}", request.productId());

        return self.getProductWithResilience(request.productId())
                .thenCompose(product -> {
                    BigDecimal exchangeRate = getExchangeRateWithResilience();
                    return self.processSaleWithResilience(request.productId())
                            .thenApply(orderId -> {
                                int bonusValue = calculateBonusValue(product.price(), request.quantity());
                                processFidelityBonusAsync(request.userId(), bonusValue);
                                return new PurchaseResponse(
                                        orderId,
                                        request.productId(),
                                        request.quantity(),
                                        product.price(),
                                        product.price().multiply(exchangeRate),
                                        0,
                                        "COMPLETED"
                                );
                            });
                })
                .exceptionally(ex -> {
                    logger.error("Exception in processPurchase: {}", ex.toString());
                    return new PurchaseResponse(null, request.productId(), request.quantity(), null, null, 0, "FAILED");
                });
    }

    @CircuitBreaker(
            name = "storeProduct",
            fallbackMethod = "fallbackProduct"
    )
    @TimeLimiter(name = "storeProduct") // 500ms de timeout
    public CompletableFuture<ProductResponse> getProductWithResilience(Long productId) {
        return CompletableFuture.supplyAsync(() -> {
            String url = storeUrl + "/product/" + productId;
            try {
                ProductResponse p = restTemplate.getForObject(url, ProductResponse.class);
                if (p == null) {
                    throw new RuntimeException("Product is null from store");
                }
                return p;
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve product with id " + productId, e);
            }
        });
    }

    private CompletableFuture<ProductResponse> fallbackProduct(Long productId, Throwable t) {
        logger.warn("Falling back to default product. productId={}, error={}", productId, t.toString());
        ProductResponse defaultProduct = new ProductResponse(
                productId,
                "Default Product",
                BigDecimal.valueOf(9999.99)
        );
        return CompletableFuture.completedFuture(defaultProduct);
    }

    private BigDecimal getExchangeRateWithResilience() {
        try {
            Double rate = restTemplate.getForObject(exchangeUrl + "/exchange", Double.class);
            if (rate != null && rate > 0) {
                lastKnownRate = BigDecimal.valueOf(rate);
            }
        } catch (Exception e) {
            logger.warn("Exchange failed => using lastKnownRate={}. Exception={}", lastKnownRate, e.toString());
        }
        return lastKnownRate;
    }

    @CircuitBreaker(
            name = "storeSell",
            fallbackMethod = "fallbackSell"
    )
    @TimeLimiter(name = "storeSell") // 500ms de timeout
    public CompletableFuture<Long> processSaleWithResilience(Long productId) {
        return CompletableFuture.supplyAsync(() -> {
            String url = storeUrl + "/sell/" + productId;
            try {
                Long orderId = restTemplate.getForObject(url, Long.class);
                if (orderId == null) {
                    throw new RuntimeException("Sell returned null orderId");
                }
                return orderId;
            } catch (Exception e) {
                throw new RuntimeException("Failed to process sale for productId " + productId, e);
            }
        });
    }

    private CompletableFuture<Long> fallbackSell(Long productId, Throwable t) {
        logger.warn("Falling back to a fake sale. productId={}, error={}", productId, t.toString());
        Long fakeOrderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        return CompletableFuture.completedFuture(fakeOrderId);
    }

    private void processFidelityBonusAsync(Long userId, int bonusValue) {
        CompletableFuture.runAsync(() -> {
            if (isInDegradeMode()) {
                logger.warn("Fidelity em modo degradado => armazenando requisição para depois: user={}, bonus={}", userId, bonusValue);
                storeFidelityRequest(userId, bonusValue);
                return;
            }

            CompletableFuture<Void> fidelityTask = CompletableFuture.runAsync(() -> {
                try {
                    restTemplate.postForObject(
                            fidelityUrl + "/bonus?user=" + userId + "&bonus=" + bonusValue,
                            null,
                            Void.class
                    );
                    logger.info("Bônus de fidelidade processado com sucesso: user={}, bonus={}", userId, bonusValue);
                } catch (Exception e) {
                    logger.warn("Erro inesperado na requisição de fidelidade => armazenando para depois. user={}, bonus={}", userId, bonusValue, e);
                    storeFidelityRequest(userId, bonusValue);
                }
            }, fidelityExecutor);

            try {
                fidelityTask.get(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.warn("Timeout ao processar bônus de fidelidade => armazenando para depois. user={}, bonus={}", userId, bonusValue, e);
                storeFidelityRequest(userId, bonusValue);
            }
        }, fidelityExecutor);
    }

    private boolean isInDegradeMode() {
        LocalDateTime start = degradeModeStart.get();
        return start != null && LocalDateTime.now().isBefore(start.plusSeconds(FAILURE_DURATION_SECONDS));
    }

    private void activateDegradeMode() {
        degradeModeStart.set(LocalDateTime.now());
    }

    private void simulateTimeDelay() {
        try {
            Thread.sleep(FAILURE_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void storeFidelityRequest(Long userId, int bonusValue) {
        pendingFidelityRequests.add(new FidelityRequest(userId, bonusValue));
        logger.info("Fidelity request stored => user={}, bonus={}. Pending count={}",
                userId, bonusValue, pendingFidelityRequests.size());
    }

    private int calculateBonusValue(BigDecimal price, Integer quantity) {
        if (price == null || quantity == null) {
            return 0;
        }
        return price.multiply(BigDecimal.valueOf(quantity)).intValue();
    }

    // =====================
    // RECORDS
    // =====================
    private record ProductResponse(Long id, String name, BigDecimal price) {}
    private record FidelityRequest(Long userId, int bonus) {}
}
