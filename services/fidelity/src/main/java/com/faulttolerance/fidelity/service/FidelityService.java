package com.faulttolerance.fidelity.service;

import com.faulttolerance.fidelity.model.BonusPoints;
import com.faulttolerance.fidelity.model.UserPoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, BonusPoints> redisTemplate;

    public FidelityService(RedisTemplate<String, BonusPoints> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Async
    public CompletableFuture<BonusPoints> processBonus(Long userId, Long orderId, Double purchaseAmount) {
        // Simulate processing delay and potential failures
        try {
            Thread.sleep((long) (Math.random() * 1000));
            if (Math.random() < 0.1) { // 10% chance of failure
                throw new RuntimeException("Failed to process bonus points");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }

        // Calculate bonus points (1 point per $10 spent)
        int points = (int) (purchaseAmount / 10.0);
        BonusPoints bonusPoints = new BonusPoints(
            userId,
            orderId,
            points,
            LocalDateTime.now(),
            "PENDING"
        );

        // Store pending points in Redis for reliability
        String key = "bonus:" + userId + ":" + orderId;
        redisTemplate.opsForValue().set(key, bonusPoints);

        // Update user's pending points
        userPointsMap.compute(userId, (key1, existingPoints) -> {
            if (existingPoints == null) {
                return new UserPoints(userId, 0, points);
            }
            return new UserPoints(
                userId,
                existingPoints.totalPoints(),
                existingPoints.pendingPoints() + points
            );
        });

        logger.info("Processed {} bonus points for user {} and order {}", 
                   points, userId, orderId);

        return CompletableFuture.completedFuture(bonusPoints);
    }

    @Async
    public CompletableFuture<BonusPoints> confirmBonus(Long userId, Long orderId) {
        String key = "bonus:" + userId + ":" + orderId;
        BonusPoints pendingBonus = redisTemplate.opsForValue().get(key);

        if (pendingBonus == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("No pending bonus found")
            );
        }

        BonusPoints confirmedBonus = new BonusPoints(
            pendingBonus.userId(),
            pendingBonus.orderId(),
            pendingBonus.points(),
            LocalDateTime.now(),
            "CONFIRMED"
        );

        // Update user's total points and remove pending points
        userPointsMap.compute(userId, (key1, existingPoints) -> {
            if (existingPoints == null) {
                return new UserPoints(userId, pendingBonus.points(), 0);
            }
            return new UserPoints(
                userId,
                existingPoints.totalPoints() + pendingBonus.points(),
                existingPoints.pendingPoints() - pendingBonus.points()
            );
        });

        // Remove pending bonus from Redis
        redisTemplate.delete(key);

        logger.info("Confirmed {} bonus points for user {} and order {}", 
                   confirmedBonus.points(), userId, orderId);

        return CompletableFuture.completedFuture(confirmedBonus);
    }

    public UserPoints getUserPoints(Long userId) {
        return userPointsMap.getOrDefault(userId, new UserPoints(userId, 0, 0));
    }
}
