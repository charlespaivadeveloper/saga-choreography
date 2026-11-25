#!/usr/bin/env bash
set -euo pipefail

CLUSTER_NAME="saga-choreography"
NAMESPACE="saga"
REGISTRY="saga"
VERSION="1.0.0"

log() { echo "[$(date +'%H:%M:%S')] $*"; }
die() { echo "[ERROR] $*" >&2; exit 1; }

check_prerequisites() {
    for cmd in kind kubectl docker mvn; do
        command -v "$cmd" &>/dev/null || die "Required command not found: $cmd"
    done
    log "All prerequisites satisfied."
}

create_cluster() {
    if kind get clusters 2>/dev/null | grep -q "^${CLUSTER_NAME}$"; then
        log "Cluster '${CLUSTER_NAME}' already exists, skipping creation."
    else
        log "Creating Kind cluster '${CLUSTER_NAME}'..."
        kind create cluster --config kind-cluster.yaml
        log "Cluster created."
    fi
    kubectl cluster-info --context "kind-${CLUSTER_NAME}"
}

build_images() {
    log "Building Maven project..."
    (cd .. && mvn clean package -DskipTests -q)

    log "Building Docker images..."
    docker build -t "${REGISTRY}/order-service:${VERSION}" \
        -f ../order-service/order-container/Dockerfile \
        ../order-service/order-container

    docker build -t "${REGISTRY}/payment-service:${VERSION}" \
        -f ../payment-service/payment-container/Dockerfile \
        ../payment-service/payment-container

    docker build -t "${REGISTRY}/inventory-service:${VERSION}" \
        -f ../inventory-service/inventory-container/Dockerfile \
        ../inventory-service/inventory-container

    log "Images built."
}

load_images() {
    log "Loading images into Kind cluster..."
    kind load docker-image "${REGISTRY}/order-service:${VERSION}"     --name "${CLUSTER_NAME}"
    kind load docker-image "${REGISTRY}/payment-service:${VERSION}"   --name "${CLUSTER_NAME}"
    kind load docker-image "${REGISTRY}/inventory-service:${VERSION}" --name "${CLUSTER_NAME}"
    log "Images loaded."
}

deploy_infrastructure() {
    log "Deploying namespace and infrastructure..."
    kubectl apply -f namespace.yaml

    log "Deploying Kafka..."
    kubectl apply -f kafka/kafka.yaml
    kubectl rollout status deployment/zookeeper -n "${NAMESPACE}" --timeout=120s
    kubectl rollout status deployment/kafka     -n "${NAMESPACE}" --timeout=120s

    log "Deploying PostgreSQL..."
    kubectl apply -f postgres/postgres-order.yaml
    kubectl apply -f postgres/postgres-payment.yaml
    kubectl apply -f postgres/postgres-inventory.yaml
    kubectl rollout status statefulset/postgres-order     -n "${NAMESPACE}" --timeout=120s
    kubectl rollout status statefulset/postgres-payment   -n "${NAMESPACE}" --timeout=120s
    kubectl rollout status statefulset/postgres-inventory -n "${NAMESPACE}" --timeout=120s

    log "Infrastructure ready."
}

deploy_services() {
    log "Deploying microservices..."
    kubectl apply -f services/order-service.yaml
    kubectl apply -f services/payment-service.yaml
    kubectl apply -f services/inventory-service.yaml

    kubectl rollout status deployment/order-service     -n "${NAMESPACE}" --timeout=180s
    kubectl rollout status deployment/payment-service   -n "${NAMESPACE}" --timeout=180s
    kubectl rollout status deployment/inventory-service -n "${NAMESPACE}" --timeout=180s

    log "Services deployed."
}

deploy_monitoring() {
    log "Deploying monitoring stack..."
    kubectl apply -f monitoring/prometheus.yaml
    kubectl apply -f monitoring/grafana.yaml
    log "Monitoring deployed."
}

deploy_kong() {
    log "Deploying Kong API Gateway..."
    kubectl apply -f kong/kong.yaml
    log "Waiting for Kong to be ready..."
    kubectl rollout status deployment/kong -n kong --timeout=180s
    log "Kong ready."
}

print_endpoints() {
    log "=== Deployment Complete ==="
    echo ""
    echo "Kong API Gateway (port 80):"
    echo "  POST   http://localhost/api/v1/orders"
    echo "  GET    http://localhost/api/v1/orders/{id}"
    echo "  POST   http://localhost/api/v1/payments/user"
    echo "  GET    http://localhost/api/v1/inventory"
    echo ""
    echo "Direct NodePort (bypass Kong — debug only):"
    echo "  Order:     http://localhost:8080/api/v1/orders"
    echo "  Payment:   http://localhost:8081/api/v1/payments"
    echo "  Inventory: http://localhost:8082/api/v1/inventory"
    echo ""
    echo "Monitoring:"
    echo "  Prometheus: http://localhost:9090"
    echo "  Grafana:    http://localhost:3000  (admin/admin)"
    echo ""
    echo "Kong Admin API (read-only in DB-less mode):"
    echo "  kubectl port-forward svc/kong-admin 8001:8001 -n kong"
    echo "  curl http://localhost:8001/routes"
    echo ""
    echo "Kafka UI:"
    echo "  kubectl port-forward svc/kafka-ui 8090:8080 -n ${NAMESPACE}"
    echo "  http://localhost:8090"
    echo ""
    echo "Destroy cluster:"
    echo "  kind delete cluster --name ${CLUSTER_NAME}"
}

main() {
    local cmd="${1:-all}"
    case "$cmd" in
        cluster)    check_prerequisites; create_cluster ;;
        build)      build_images ;;
        load)       load_images ;;
        infra)      deploy_infrastructure ;;
        services)   deploy_services ;;
        kong)       deploy_kong ;;
        monitoring) deploy_monitoring ;;
        all)
            check_prerequisites
            create_cluster
            build_images
            load_images
            deploy_infrastructure
            deploy_services
            deploy_kong
            deploy_monitoring
            print_endpoints
            ;;
        *)
            echo "Usage: $0 [cluster|build|load|infra|services|kong|monitoring|all]"
            exit 1
            ;;
    esac
}

main "$@"
