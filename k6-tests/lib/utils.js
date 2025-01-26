import { check } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
export const errorRate = new Rate('errors');
export const successRate = new Rate('success');

// Configuration
export const BASE_URL = 'http://localhost:8080';

// Helper functions
export function makeRequest(http, url, params = {}, ft = false) {
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };

    // Movendo os parâmetros para o corpo da requisição
    const payload = {
        ...params,
        ft: ft
    };

    const response = http.post(
        url,
        JSON.stringify(payload),
        { headers }
    );
    
    // Check if request was successful
    const success = check(response, {
        'status is 200': (r) => r.status === 200,
        'response time < 2000ms': (r) => r.timings.duration < 2000,
    });

    // Update custom metrics
    errorRate.add(!success);
    successRate.add(success);

    return response;
}

// Common options for all tests
export const commonOptions = {
    thresholds: {
        http_req_duration: ['p(95)<2000'], // 95% of requests should complete within 2s
        'http_req_duration{status:200}': ['max<3000'], // No successful request should take more than 3s
        errors: ['rate<0.1'], // Error rate should be less than 10%
        success: ['rate>0.9'], // Success rate should be more than 90%
    },
    noConnectionReuse: true,
    userAgent: 'K6 Performance Tests',
};

// Common setup function
export function setup() {
    return {
        productId: 1,
        userId: 1,
    };
}

// Common teardown function
export function teardown(data) {
    // Could be used for cleanup if needed
}
