package com.faulttolerance.exchange.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            String pong = connection.ping();
            
            if ("PONG".equals(pong)) {
                return Health.up()
                    .withDetail("redis", "UP")
                    .withDetail("ping_response", pong)
                    .build();
            } else {
                return Health.down()
                    .withDetail("redis", "DOWN")
                    .withDetail("ping_response", pong)
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("redis", "DOWN")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
