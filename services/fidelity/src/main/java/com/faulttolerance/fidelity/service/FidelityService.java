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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FidelityService {
    private static final Logger logger = LoggerFactory.getLogger(FidelityService.class);
    private final Map<Long, UserPoints> userPointsMap = new ConcurrentHashMap<>();
    private final BonusPointsRepository bonusPointsRepository;

    public FidelityService(BonusPointsRepository bonusPointsRepository) {
        this.bonusPointsRepository = bonusPointsRepository;
    }

    @Async
    public CompletableFuture<BonusPoints> processBonus(Long userId, Long orderId, Double purchaseAmount) {
        try {
            Thread.sleep((long) (Math.random() * 1000));
            if (Math.random() < 0.1) {
                throw new RuntimeException("Failed to process bonus points");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
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
}
