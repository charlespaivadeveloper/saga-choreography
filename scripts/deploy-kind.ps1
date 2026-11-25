#Requires -Version 5.1
# Full deployment to kind: builds images, creates cluster, deploys all services + Kong.

param(
    [switch]$SkipBuild,
    [switch]$SkipCluster
)

$ErrorActionPreference = "Stop"
$CLUSTER_NAME = "saga-choreography"
$ROOT_DIR     = Split-Path $PSScriptRoot -Parent
$K8S_DIR      = Join-Path $ROOT_DIR "k8s"

function Log   { param($msg) Write-Host "[INFO]  $msg" -ForegroundColor Cyan }
function Ok    { param($msg) Write-Host "[OK]    $msg" -ForegroundColor Green }
function Warn  { param($msg) Write-Host "[WARN]  $msg" -ForegroundColor Yellow }
function Err   { param($msg) Write-Host "[ERROR] $msg" -ForegroundColor Red; exit 1 }

function Test-Command { param($cmd) return [bool](Get-Command $cmd -ErrorAction SilentlyContinue) }

function Assert-Prereqs {
    Log "Checking prerequisites..."
    foreach ($cmd in @("kind", "kubectl", "docker", "mvn")) {
        if (-not (Test-Command $cmd)) { Err "'$cmd' not found — please install it first." }
    }
    Ok "All prerequisites met."
}

function New-KindCluster {
    if ($SkipCluster) { Warn "Skipping cluster creation (-SkipCluster)."; return }

    $existing = kind get clusters 2>$null
    if ($existing -contains $CLUSTER_NAME) {
        Warn "Cluster '$CLUSTER_NAME' already exists — skipping creation."
    } else {
        Log "Creating kind cluster '$CLUSTER_NAME'..."
        kind create cluster --config "$K8S_DIR\kind-cluster.yaml"
        Ok "Cluster '$CLUSTER_NAME' created."
    }
    kubectl config use-context "kind-$CLUSTER_NAME"
}

function Build-AndLoadImages {
    if ($SkipBuild) { Warn "Skipping build (-SkipBuild)."; return }

    Log "Building Maven project (skipping tests)..."
    mvn -f "$ROOT_DIR\pom.xml" clean package -DskipTests -q
    if ($LASTEXITCODE -ne 0) { Err "Maven build failed." }
    Ok "Maven build complete."

    Log "Building Docker images..."
    $services = @(
        @{ Tag = "saga/order-service:1.0.0";     Dockerfile = "$ROOT_DIR\order-service\order-container\Dockerfile";     Context = "$ROOT_DIR\order-service\order-container" },
        @{ Tag = "saga/payment-service:1.0.0";   Dockerfile = "$ROOT_DIR\payment-service\payment-container\Dockerfile"; Context = "$ROOT_DIR\payment-service\payment-container" },
        @{ Tag = "saga/inventory-service:1.0.0"; Dockerfile = "$ROOT_DIR\inventory-service\inventory-container\Dockerfile"; Context = "$ROOT_DIR\inventory-service\inventory-container" }
    )

    foreach ($svc in $services) {
        docker build -t $svc.Tag -f $svc.Dockerfile $svc.Context
        if ($LASTEXITCODE -ne 0) { Err "Docker build failed for $($svc.Tag)." }
    }
    Ok "Docker images built."

    Log "Loading images into kind..."
    foreach ($svc in $services) {
        kind load docker-image $svc.Tag --name $CLUSTER_NAME
    }
    Ok "Images loaded into kind."
}

function Deploy-Infra {
    Log "Applying namespace..."
    kubectl apply -f "$K8S_DIR\namespace.yaml"

    Log "Deploying PostgreSQL instances..."
    kubectl apply -f "$K8S_DIR\postgres\"

    Log "Deploying Kafka + Zookeeper..."
    kubectl apply -f "$K8S_DIR\kafka\"

    Log "Waiting for PostgreSQL pods (up to 3 min)..."
    kubectl wait pod -l app=postgres-order     -n saga --for=condition=Ready --timeout=180s
    kubectl wait pod -l app=postgres-payment   -n saga --for=condition=Ready --timeout=180s
    kubectl wait pod -l app=postgres-inventory -n saga --for=condition=Ready --timeout=180s
    Ok "PostgreSQL ready."

    Log "Waiting for Kafka (up to 3 min)..."
    kubectl wait pod -l app=kafka -n saga --for=condition=Ready --timeout=180s
    Ok "Kafka ready."
}

function Deploy-Services {
    Log "Deploying microservices..."
    kubectl apply -f "$K8S_DIR\services\"

    foreach ($svc in @("order-service", "payment-service", "inventory-service")) {
        Log "Waiting for $svc (up to 5 min)..."
        kubectl rollout status "deployment/$svc" -n saga --timeout=300s
    }
    Ok "All microservices ready."
}

function Deploy-Kong {
    Log "Deploying Kong API Gateway..."
    kubectl apply -f "$K8S_DIR\kong\"

    Log "Waiting for Kong (up to 3 min)..."
    kubectl rollout status deployment/kong -n kong --timeout=180s
    Ok "Kong ready."
}

function Deploy-Monitoring {
    Log "Deploying Prometheus + Grafana..."
    kubectl apply -f "$K8S_DIR\monitoring\"
    Ok "Monitoring deployed."
}

function Write-Summary {
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "   SAGA Choreography v2 — running on kind" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Kong API Gateway (port 80):" -ForegroundColor White
    Write-Host "    POST   http://localhost/api/v1/orders"
    Write-Host "    GET    http://localhost/api/v1/orders/{id}"
    Write-Host "    POST   http://localhost/api/v1/payments/user"
    Write-Host "    GET    http://localhost/api/v1/inventory"
    Write-Host ""
    Write-Host "  Direct NodePort (bypass Kong — debug only):" -ForegroundColor White
    Write-Host "    Order:     http://localhost:8080/api/v1/orders"
    Write-Host "    Payment:   http://localhost:8081/api/v1/payments"
    Write-Host "    Inventory: http://localhost:8082/api/v1/inventory"
    Write-Host ""
    Write-Host "  Monitoring:" -ForegroundColor White
    Write-Host "    Prometheus: http://localhost:9090"
    Write-Host "    Grafana:    http://localhost:3000  (admin / admin)"
    Write-Host ""
    Write-Host "  Kong Admin API (read-only in DB-less mode):" -ForegroundColor White
    Write-Host "    kubectl port-forward svc/kong-admin 8001:8001 -n kong"
    Write-Host "    Invoke-RestMethod http://localhost:8001/routes"
    Write-Host ""
    Write-Host "  Kafka UI:" -ForegroundColor White
    Write-Host "    kubectl port-forward svc/kafka-ui 8090:8080 -n saga"
    Write-Host "    http://localhost:8090"
    Write-Host ""
    Write-Host "  Destroy cluster:" -ForegroundColor White
    Write-Host "    kind delete cluster --name $CLUSTER_NAME"
    Write-Host "============================================================" -ForegroundColor Cyan
}

Assert-Prereqs
New-KindCluster
Build-AndLoadImages
Deploy-Infra
Deploy-Services
Deploy-Kong
Deploy-Monitoring
Write-Summary
