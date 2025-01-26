import http from 'k6/http';
import { sleep } from 'k6';
import { group } from 'k6';
import { commonOptions, makeRequest, BASE_URL, setup, teardown } from '../lib/utils.js';

export { setup, teardown };

// Test configuration
export const options = {
    ...commonOptions,
    stages: [
        { duration: '2m', target: 50 },  // Ramp up to 50 users over 2 minutes
        { duration: '2m', target: 50 },  // Stay at 50 users for 2 minutes
        { duration: '1m', target: 0 },   // Ramp down to 0 users over 1 minute
    ],
    tags: { testType: 'load' },
};

// Default function that defines VU behavior
export default function (data) {
    const params = {
        product: 1,  // ID do produto (1 = Laptop)
        user: 1      // ID do usuário
    };

    group('Without Fault Tolerance', function () {
        makeRequest(http, `${BASE_URL}/buy`, params, false);
    });
    
    sleep(1);  // Wait 1 second between iterations
    
    group('With Fault Tolerance', function () {
        makeRequest(http, `${BASE_URL}/buy`, params, true);
    });
    
    sleep(1);  // Wait 1 second between iterations
}
