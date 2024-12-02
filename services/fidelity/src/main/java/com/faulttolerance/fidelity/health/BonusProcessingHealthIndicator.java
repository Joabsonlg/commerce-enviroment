package com.faulttolerance.fidelity.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BonusProcessingHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    public BonusProcessingHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            // Check Redis connection
            boolean redisConnected = redisTemplate.getConnectionFactory().getConnection().ping() != null;
            
            // Get pending bonus count
            Set<String> pendingKeys = redisTemplate.keys("bonus:*");
            int pendingCount = pendingKeys != null ? pendingKeys.size() : 0;
            
            Health.Builder health = Health.up()
                .withDetail("redis_connected", redisConnected)
                .withDetail("pending_bonus_count", pendingCount);

            // If there are too many pending bonuses, mark as DOWN
            if (pendingCount > 1000) {
                health.down()
                    .withDetail("status", "Too many pending bonus points")
                    .withDetail("threshold", 1000);
            }

            return health.build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
