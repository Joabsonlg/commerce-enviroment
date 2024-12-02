package com.faulttolerance.ecommerce.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExternalServicesHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final String storeUrl;
    private final String exchangeUrl;
    private final String fidelityUrl;

    public ExternalServicesHealthIndicator(
            RestTemplate restTemplate,
            @Value("${store.url}") String storeUrl,
            @Value("${exchange.url}") String exchangeUrl,
            @Value("${fidelity.url}") String fidelityUrl) {
        this.restTemplate = restTemplate;
        this.storeUrl = storeUrl;
        this.exchangeUrl = exchangeUrl;
        this.fidelityUrl = fidelityUrl;
    }

    @Override
    public Health health() {
        Health.Builder health = new Health.Builder();

        // Check Store Service
        try {
            restTemplate.getForObject(storeUrl + "/actuator/health", String.class);
            health.withDetail("store", "UP");
        } catch (Exception e) {
            health.withDetail("store", "DOWN")
                 .withDetail("store.error", e.getMessage());
            health.down();
        }

        // Check Exchange Service
        try {
            restTemplate.getForObject(exchangeUrl + "/actuator/health", String.class);
            health.withDetail("exchange", "UP");
        } catch (Exception e) {
            health.withDetail("exchange", "DOWN")
                 .withDetail("exchange.error", e.getMessage());
            health.down();
        }

        // Check Fidelity Service
        try {
            restTemplate.getForObject(fidelityUrl + "/actuator/health", String.class);
            health.withDetail("fidelity", "UP");
        } catch (Exception e) {
            health.withDetail("fidelity", "DOWN")
                 .withDetail("fidelity.error", e.getMessage());
            health.down();
        }

        return health.build();
    }
}
