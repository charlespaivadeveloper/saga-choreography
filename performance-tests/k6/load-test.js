import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter, Trend, Gauge } from 'k6/metrics';

const errorRate = new Rate('errors');
const orderCreationTrend = new Trend('order_creation_duration', true);
const successfulOrders = new Counter('successful_orders');
const failedOrders = new Counter('failed_orders');
const activeVUs = new Gauge('active_vus');

export const options = {
    stages: [
        { duration: '2m', target: 10 },
        { duration: '5m', target: 10 },
        { duration: '2m', target: 20 },
        { duration: '5m', target: 20 },
        { duration: '2m', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(50)<200', 'p(90)<500', 'p(95)<800', 'p(99)<1500'],
        errors: ['rate<0.05'],
        order_creation_duration: ['p(95)<1000'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const SCENARIOS = [
    { userId: 1, productId: 1 },
    { userId: 1, productId: 2 },
    { userId: 2, productId: 1 },
    { userId: 2, productId: 3 },
    { userId: 3, productId: 2 },
    { userId: 3, productId: 3 },
];

function randomScenario() {
    return SCENARIOS[Math.floor(Math.random() * SCENARIOS.length)];
}

export default function () {
    activeVUs.add(1);

    const scenario = randomScenario();
    const payload = JSON.stringify(scenario);
    const params = {
        headers: { 'Content-Type': 'application/json' },
        timeout: '15s',
        tags: { scenario: `user${scenario.userId}_product${scenario.productId}` },
    };

    const startTime = Date.now();
    const res = http.post(`${BASE_URL}/api/v1/orders`, payload, params);
    const duration = Date.now() - startTime;
    orderCreationTrend.add(duration);

    const success = check(res, {
        'status 201': (r) => r.status === 201,
        'response time < 1s': () => duration < 1000,
        'valid body': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.orderId && body.orderStatus;
            } catch {
                return false;
            }
        },
    });

    if (success) {
        successfulOrders.add(1);
    } else {
        failedOrders.add(1);
        errorRate.add(1);
    }

    // Also test the GET endpoint
    const listRes = http.get(`${BASE_URL}/api/v1/orders`, {
        timeout: '10s',
        tags: { endpoint: 'list' },
    });

    check(listRes, {
        'list status 200': (r) => r.status === 200,
        'list has orders': (r) => {
            try {
                return Array.isArray(JSON.parse(r.body).orders);
            } catch {
                return false;
            }
        },
    });

    sleep(Math.random() * 2 + 0.5);
}

export function handleSummary(data) {
    return {
        'results/load-test-summary.json': JSON.stringify(data, null, 2),
        stdout: textSummary(data, { indent: ' ', enableColors: true }),
    };
}

function textSummary(data, _opts) {
    const dur = data.metrics.http_req_duration;
    if (!dur) return 'No metrics collected.';
    return [
        `\n=== Load Test Summary ===`,
        `Total requests  : ${data.metrics.http_reqs?.values?.count ?? 'N/A'}`,
        `Error rate      : ${(data.metrics.errors?.values?.rate * 100 ?? 0).toFixed(2)}%`,
        `P50 duration    : ${dur.values?.['p(50)']?.toFixed(2) ?? 'N/A'} ms`,
        `P95 duration    : ${dur.values?.['p(95)']?.toFixed(2) ?? 'N/A'} ms`,
        `P99 duration    : ${dur.values?.['p(99)']?.toFixed(2) ?? 'N/A'} ms`,
        `Successful orders: ${data.metrics.successful_orders?.values?.count ?? 0}`,
        `Failed orders   : ${data.metrics.failed_orders?.values?.count ?? 0}`,
        `========================\n`,
    ].join('\n');
}
