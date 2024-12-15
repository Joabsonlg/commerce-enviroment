package com.faulttolerance.exchange.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

@Component
public class MongoHealthIndicator implements HealthIndicator {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoHealthIndicator(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Health health() {
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            return Health.up()
                    .withDetail("mongo", "UP")
                    .withDetail("ping_response", "Command executed successfully")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("mongo", "DOWN")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}