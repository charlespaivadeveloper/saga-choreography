import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter, Trend } from 'k6/metrics';

const errorRate = new Rate('errors');
const orderCreationTrend = new Trend('order_creation_duration', true);
const successfulOrders = new Counter('successful_orders');
const failedOrders = new Counter('failed_orders');

export const options = {
    stages: [
        { duration: '2m', target: 50 },
        { duration: '5m', target: 50 },
        { duration: '2m', target: 100 },
        { duration: '5m', target: 100 },
        { duration: '2m', target: 200 },
        { duration: '5m', target: 200 },
        { duration: '5m', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.10'],
        http_req_duration: ['p(95)<3000'],
        errors: ['rate<0.10'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
    const userId = Math.floor(Math.random() * 3) + 1;
    const productId = Math.floor(Math.random() * 3) + 1;

    const payload = JSON.stringify({ userId, productId });
    const params = {
        headers: { 'Content-Type': 'application/json' },
        timeout: '30s',
    };

    const startTime = Date.now();
    const res = http.post(`${BASE_URL}/api/v1/orders`, payload, params);
    orderCreationTrend.add(Date.now() - startTime);

    const success = check(res, {
        'status 2xx': (r) => r.status >= 200 && r.status < 300,
        'not 5xx': (r) => r.status < 500,
    });

    if (success && res.status === 201) {
        successfulOrders.add(1);
    } else {
        failedOrders.add(1);
        if (!success) errorRate.add(1);
    }

    sleep(0.1);
}

export function handleSummary(data) {
    return {
        'results/stress-test-summary.json': JSON.stringify(data, null, 2),
    };
}
