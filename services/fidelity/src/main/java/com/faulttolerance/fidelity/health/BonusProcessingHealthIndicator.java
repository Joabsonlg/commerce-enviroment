package com.faulttolerance.fidelity.health;

import com.faulttolerance.fidelity.repository.BonusPointsRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class BonusProcessingHealthIndicator implements HealthIndicator {

    private final BonusPointsRepository bonusPointsRepository;

    public BonusProcessingHealthIndicator(BonusPointsRepository bonusPointsRepository) {
        this.bonusPointsRepository = bonusPointsRepository;
    }

    @Override
    public Health health() {
        try {
            long count = bonusPointsRepository.count();

            long pendingCount = bonusPointsRepository.countByStatus("PENDING");

            Health.Builder health = Health.up()
                    .withDetail("mongo_connected", true)
                    .withDetail("total_bonus_count", count)
                    .withDetail("pending_bonus_count", pendingCount);

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
