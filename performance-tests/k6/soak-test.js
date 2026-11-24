import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter, Trend } from 'k6/metrics';

const errorRate = new Rate('errors');
const orderCreationTrend = new Trend('order_creation_duration', true);
const successfulOrders = new Counter('successful_orders');

export const options = {
    stages: [
        { duration: '5m', target: 10 },
        { duration: '8h', target: 10 },
        { duration: '5m', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
        errors: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
    const userId = Math.floor(Math.random() * 3) + 1;
    const productId = Math.floor(Math.random() * 3) + 1;

    const createRes = http.post(
        `${BASE_URL}/api/v1/orders`,
        JSON.stringify({ userId, productId }),
        { headers: { 'Content-Type': 'application/json' }, timeout: '10s' }
    );

    const createOk = check(createRes, {
        'create: status 201': (r) => r.status === 201,
        'create: has orderId': (r) => {
            try { return !!JSON.parse(r.body).orderId; } catch { return false; }
        },
    });

    if (createOk) successfulOrders.add(1);
    else errorRate.add(1);

    const listRes = http.get(`${BASE_URL}/api/v1/orders`, { timeout: '10s' });
    check(listRes, {
        'list: status 200': (r) => r.status === 200,
    });

    orderCreationTrend.add(createRes.timings.duration);

    sleep(2);
}

export function handleSummary(data) {
    return {
        'results/soak-test-summary.json': JSON.stringify(data, null, 2),
    };
}
