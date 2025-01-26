import http from 'k6/http';
import { sleep } from 'k6';
import { group } from 'k6';
import { commonOptions, makeRequest, BASE_URL, setup, teardown } from '../lib/utils.js';

export { setup, teardown };

// Test configuration
export const options = {
    ...commonOptions,
    stages: [
        { duration: '5m', target: 200 },  // Ramp up to 200 users over 5 minutes
        { duration: '3m', target: 200 },  // Stay at 200 users for 3 minutes
        { duration: '2m', target: 0 },    // Ramp down to 0 users over 2 minutes
    ],
    tags: { testType: 'stress' },
    // More aggressive thresholds for stress test
    thresholds: {
        ...commonOptions.thresholds,
        http_req_duration: ['p(95)<3000'], // 95% of requests should complete within 3s
        'http_req_duration{status:200}': ['max<5000'], // No successful request should take more than 5s
        errors: ['rate<0.2'], // Error rate should be less than 20% under stress
        success: ['rate>0.8'], // Success rate should be more than 80% under stress
    },
};

// Default function that defines VU behavior
export default function (data) {
    group('Without Fault Tolerance - High Load', function () {
        makeRequest(http, `${BASE_URL}/buy`, {
            product: data.productId,
            user: data.userId
        }, false);
    });
    
    sleep(0.5);  // Reduced sleep time for higher load
    
    group('With Fault Tolerance - High Load', function () {
        makeRequest(http, `${BASE_URL}/buy`, {
            product: data.productId,
            user: data.userId
        }, true);
    });
    
    sleep(0.5);  // Reduced sleep time for higher load
}
