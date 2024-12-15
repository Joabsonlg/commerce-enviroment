package com.faulttolerance.store.health;

import com.faulttolerance.store.model.Product;
import com.faulttolerance.store.service.ProductService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductInventoryHealthIndicator implements HealthIndicator {

    private final ProductService productService;
    private static final int LOW_STOCK_THRESHOLD = 5;

    public ProductInventoryHealthIndicator(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public Health health() {
        try {
            List<Product> products = List.of(productService.getProduct(1L));

            Map<Long, Integer> lowStockProducts = products.stream()
                .filter(p -> p.stock() <= LOW_STOCK_THRESHOLD)
                .collect(Collectors.toMap(
                    Product::id,
                    Product::stock
                ));

            Health.Builder health = Health.up()
                .withDetail("total_products", products.size())
                .withDetail("low_stock_count", lowStockProducts.size());

            if (!lowStockProducts.isEmpty()) {
                health.withDetail("low_stock_products", lowStockProducts);

                if (lowStockProducts.size() > products.size() / 2) {
                    health.down()
                        .withDetail("status", "Critical inventory levels");
                }
            }

            return health.build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
