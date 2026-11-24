import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter, Trend } from 'k6/metrics';

const errorRate = new Rate('errors');
const orderCreationTrend = new Trend('order_creation_duration');
const successfulOrders = new Counter('successful_orders');

export const options = {
    vus: 1,
    duration: '1m',
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
        errors: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const USERS = [1, 2, 3];
const PRODUCTS = [1, 2, 3];

function randomFrom(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export default function () {
    const payload = JSON.stringify({
        userId: randomFrom(USERS),
        productId: randomFrom(PRODUCTS),
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
        timeout: '10s',
    };

    const startTime = Date.now();
    const res = http.post(`${BASE_URL}/api/v1/orders`, payload, params);
    orderCreationTrend.add(Date.now() - startTime);

    const success = check(res, {
        'status is 201': (r) => r.status === 201,
        'has orderId': (r) => {
            try {
                return JSON.parse(r.body).orderId !== undefined;
            } catch {
                return false;
            }
        },
        'has orderStatus': (r) => {
            try {
                return JSON.parse(r.body).orderStatus === 'ORDER_CREATED';
            } catch {
                return false;
            }
        },
    });

    errorRate.add(!success);
    if (success) {
        successfulOrders.add(1);
    }

    sleep(1);
}

export function handleSummary(data) {
    return {
        'results/smoke-test-summary.json': JSON.stringify(data, null, 2),
    };
}
