package com.faulttolerance.fidelity.service;

import com.faulttolerance.fidelity.model.BonusPoints;
import com.faulttolerance.fidelity.model.UserPoints;
import com.faulttolerance.fidelity.repository.BonusPointsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class FidelityService {
    private static final Logger logger = LoggerFactory.getLogger(FidelityService.class);
    private final Map<Long, UserPoints> userPointsMap = new ConcurrentHashMap<>();
    private final BonusPointsRepository bonusPointsRepository;

    private final AtomicReference<LocalDateTime> degradeModeStart = new AtomicReference<>(null);

    private final BlockingQueue<BonusRequest> pendingRequests = new LinkedBlockingQueue<>();

    private static final long DEGRADE_DURATION_SECONDS = 30L;

    public FidelityService(BonusPointsRepository bonusPointsRepository) {
        this.bonusPointsRepository = bonusPointsRepository;
    }

    @Async
    public CompletableFuture<BonusPoints> processBonus(Long userId, Long orderId, Double purchaseAmount) {
        if (isInDegradeMode()) {
            simulateTimeDelay();
            logger.warn("System in degraded mode: storing processing requests for later...");
            BonusRequest request = new BonusRequest(userId, orderId, purchaseAmount);
            pendingRequests.add(request);
            return CompletableFuture.completedFuture(null);
        }

        if (Math.random() < 0.1) {
            logger.error("TIME type fault detected. Entering degraded mode for 30s...");
            activateDegradeMode();
            simulateTimeDelay();
            BonusRequest request = new BonusRequest(userId, orderId, purchaseAmount);
            pendingRequests.add(request);
            return CompletableFuture.completedFuture(null);
        }

        int points = (int) (purchaseAmount / 10.0);
        BonusPoints bonusPoints = new BonusPoints();
        bonusPoints.setUserId(userId);
        bonusPoints.setOrderId(orderId);
        bonusPoints.setPoints(points);
        bonusPoints.setCreatedDate(LocalDateTime.now());
        bonusPoints.setStatus("PENDING");

        bonusPointsRepository.save(bonusPoints);

        userPointsMap.compute(userId, (key, existingPoints) -> {
            if (existingPoints == null) {
                return new UserPoints(userId, 0, points);
            }
            return new UserPoints(
                    userId,
                    existingPoints.totalPoints(),
                    existingPoints.pendingPoints() + points
            );
        });

        logger.info("Processed {} bonus points for user {} and order {}", points, userId, orderId);

        return CompletableFuture.completedFuture(bonusPoints);
    }

    @Async
    public CompletableFuture<BonusPoints> confirmBonus(Long userId, Long orderId) {
        BonusPoints pendingBonus = bonusPointsRepository.findByUserIdAndOrderId(userId, orderId);

        if (pendingBonus == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("No pending bonus found"));
        }

        pendingBonus.setStatus("CONFIRMED");
        pendingBonus.setCreatedDate(LocalDateTime.now());

        bonusPointsRepository.save(pendingBonus);

        userPointsMap.compute(userId, (key, existingPoints) -> {
            if (existingPoints == null) {
                return new UserPoints(userId, pendingBonus.getPoints(), 0);
            }
            return new UserPoints(
                    userId,
                    existingPoints.totalPoints() + pendingBonus.getPoints(),
                    existingPoints.pendingPoints() - pendingBonus.getPoints()
            );
        });

        logger.info("Confirmed {} bonus points for user {} and order {}", pendingBonus.getPoints(), userId, orderId);

        return CompletableFuture.completedFuture(pendingBonus);
    }

    public UserPoints getUserPoints(Long userId) {
        return userPointsMap.getOrDefault(userId, new UserPoints(userId, 0, 0));
    }

    private void activateDegradeMode() {
        degradeModeStart.set(LocalDateTime.now());
    }

    private boolean isInDegradeMode() {
        LocalDateTime start = degradeModeStart.get();
        if (start == null) {
            return false;
        }
        LocalDateTime end = start.plusSeconds(DEGRADE_DURATION_SECONDS);
        boolean inDegrade = LocalDateTime.now().isBefore(end);
        if (!inDegrade) {
            degradeModeStart.set(null);
            reprocessPendingRequests();
        }
        return inDegrade;
    }

    private void simulateTimeDelay() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void reprocessPendingRequests() {
        logger.info("Exiting degraded mode. Reprocessing pending requests...");
        BonusRequest req;
        while ((req = pendingRequests.poll()) != null) {
            try {
                int points = (int) (req.purchaseAmount / 10.0);
                BonusPoints bonusPoints = new BonusPoints();
                bonusPoints.setUserId(req.userId);
                bonusPoints.setOrderId(req.orderId);
                bonusPoints.setPoints(points);
                bonusPoints.setCreatedDate(LocalDateTime.now());
                bonusPoints.setStatus("PENDING");

                bonusPointsRepository.save(bonusPoints);

                BonusRequest finalReq = req;
                userPointsMap.compute(req.userId, (key, existingPoints) -> {
                    if (existingPoints == null) {
                        return new UserPoints(finalReq.userId, 0, points);
                    }
                    return new UserPoints(
                            finalReq.userId,
                            existingPoints.totalPoints(),
                            existingPoints.pendingPoints() + points
                    );
                });

                logger.info("Reprocessed {} bonus points for user {} and order {}",
                        points, req.userId, req.orderId);

            } catch (Exception e) {
                logger.error("Failed to reprocess pending request: {}", e.getMessage());
            }
        }
    }

    private static class BonusRequest {
        Long userId;
        Long orderId;
        Double purchaseAmount;

        public BonusRequest(Long userId, Long orderId, Double purchaseAmount) {
            this.userId = userId;
            this.orderId = orderId;
            this.purchaseAmount = purchaseAmount;
        }
    }
}
