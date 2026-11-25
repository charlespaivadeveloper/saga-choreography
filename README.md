# SAGA Choreography v2 — Clean Architecture + DDD

Sistema de microsserviços Java 21 / Spring Boot 3.3.5 implementando o padrão **SAGA Choreography** com **Clean Architecture**, **Domain-Driven Design (DDD)**, **SOLID** e **GRASP**.

---

## Índice

1. [Visão Geral](#1-visão-geral)
2. [Arquitetura](#2-arquitetura)
   - [As Quatro Camadas (Uncle Bob)](#21-as-quatro-camadas-uncle-bob)
   - [Mapeamento no Projeto](#22-mapeamento-no-projeto)
   - [A Regra de Dependência](#23-a-regra-de-dependência)
   - [Fluxo SAGA Choreography](#24-fluxo-saga-choreography)
3. [Módulos Maven](#3-módulos-maven)
4. [Stack Tecnológica](#4-stack-tecnológica)
5. [Pré-requisitos](#5-pré-requisitos)
6. [Executando Localmente](#6-executando-localmente)
7. [Endpoints e Documentação API](#7-endpoints-e-documentação-api)
8. [Kafka — Tópicos e Mensagens](#8-kafka--tópicos-e-mensagens)
9. [Banco de Dados](#9-banco-de-dados)
10. [Monitoramento e Observabilidade](#10-monitoramento-e-observabilidade)
11. [Testes](#11-testes)
12. [Testes de Performance](#12-testes-de-performance)
13. [Kubernetes](#13-kubernetes)
14. [Qualidade de Código](#14-qualidade-de-código)
15. [Variáveis de Ambiente](#15-variáveis-de-ambiente)
16. [Decisões de Design](#16-decisões-de-design)

---

## 1. Visão Geral

Os serviços comunicam-se exclusivamente via **eventos Kafka**, sem orquestrador central. Cada serviço reage a eventos de outros serviços e publica seus próprios eventos, garantindo consistência eventual distribuída.

```mermaid
flowchart TD
    Client(["fa:fa-user Client"]):::external

    Client -->|"POST /api/v1/orders"| OS

    subgraph order-service
        OS["fa:fa-server order-service\n(producer)"]
        OSL["fa:fa-server order-service\n(listener)"]
    end

    OS -->|"OrderCreatedEvent"| OT[("saga-order-events")]

    OT -->|consume| PS["fa:fa-credit-card payment-service"]
    OT -->|consume| IS["fa:fa-box inventory-service"]

    PS -->|"PaymentEventMessage\nRESERVED | REJECTED"| PT[("saga-payment-events")]
    IS -->|"InventoryEventMessage\nRESERVED | REJECTED"| IT[("saga-inventory-events")]

    PT -->|consume| OSL
    IT -->|consume| OSL

    OSL -->|"ambos RESERVED"| COMP(["fa:fa-check ORDER_COMPLETED"]):::success
    OSL -->|"qualquer REJECTED"| CANC(["fa:fa-times ORDER_CANCELLED"]):::failure

    classDef external fill:#f0f0f0,stroke:#999,color:#333
    classDef success fill:#d4edda,stroke:#28a745,color:#155724
    classDef failure fill:#f8d7da,stroke:#dc3545,color:#721c24
```

---

## 2. Arquitetura

### 2.1 As Quatro Camadas (Uncle Bob)

> *"The overriding rule that makes this architecture work is The Dependency Rule: source code dependencies can only point inwards."*
> — Robert C. Martin, [The Clean Architecture (2012)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

```
╔══════════════════════════════════════════════════════════════╗
║  4. Frameworks & Drivers                                     ║
║  ┌────────────────────────────────────────────────────────┐  ║
║  │  3. Interface Adapters                                 │  ║
║  │  ┌──────────────────────────────────────────────────┐  │  ║
║  │  │  2. Use Cases (Application Business Rules)       │  │  ║
║  │  │  ┌────────────────────────────────────────────┐  │  │  ║
║  │  │  │  1. Entities (Enterprise Business Rules)   │  │  │  ║
║  │  │  └────────────────────────────────────────────┘  │  │  ║
║  │  └──────────────────────────────────────────────────┘  │  ║
║  └────────────────────────────────────────────────────────┘  ║
╚══════════════════════════════════════════════════════════════╝
         Dependências sempre apontam para o centro →
```

| Camada | Responsabilidade | O que NÃO pode entrar |
|--------|-----------------|----------------------|
| **1 — Entities** | Regras de negócio da empresa. Os objetos mais estáveis do sistema. | Spring, JPA, Kafka, banco de dados, qualquer framework |
| **2 — Use Cases** | Regras de negócio da aplicação. Orquestra o fluxo entre entidades. | Infrastructure, banco de dados, Kafka, REST |
| **3 — Interface Adapters** | Converte dados entre o formato interno (domínio/use cases) e o externo (HTTP, BD, eventos). | Lógica de negócio |
| **4 — Frameworks & Drivers** | Web, banco de dados, Kafka, Spring Boot. "The Web is a detail." | — (camada mais externa) |

### 2.2 Mapeamento no Projeto

Cada serviço implementa as quatro camadas como módulos Maven independentes:

```
{service}-service/
│
├── {service}-domain/
│   ├── {service}-domain-core/          ← Camada 1: ENTITIES
│   │   Aggregates, Value Objects, Domain Events,
│   │   Domain Services — zero dependências de framework
│   │
│   └── {service}-application-service/  ← Camada 2: USE CASES
│       Command Handlers, Input/Output Ports (interfaces),
│       DTOs, Mappers — conhece apenas o domain-core
│
├── {service}-infrastructure/           ← Camada 3: INTERFACE ADAPTERS (persistência/mensageria)
│   JPA Entities, Repository Adapters, Kafka Publishers/Listeners
│   Implementa os Output Ports definidos em application-service
│
├── {service}-application/              ← Camada 3: INTERFACE ADAPTERS (HTTP)
│   REST Controllers, GlobalExceptionHandler
│   Delega para os Input Ports de application-service
│
└── {service}-container/                ← Camada 4: FRAMEWORKS & DRIVERS
    Spring Boot bootstrap, Bean Configuration,
    Kafka/JPA/Flyway config, Testes de integração
```

#### Exemplo concreto — Order Service

| Módulo Maven | Camada Uncle Bob | Conteúdo principal |
|---|---|---|
| `order-domain-core` | Entities | `Order` (aggregate), `OrderCreatedEvent`, `OrderDomainService` |
| `order-application-service` | Use Cases | `OrderCreateCommandHandler`, `OrderApplicationService` (port), `CreateOrderCommand` (DTO) |
| `order-infrastructure` | Interface Adapters | `OrderRepositoryImpl`, `OrderKafkaMessagePublisher`, `PaymentResponseKafkaListener` |
| `order-application` | Interface Adapters | `OrderController`, `GlobalExceptionHandler` |
| `order-container` | Frameworks & Drivers | `OrderServiceApplication`, `OrderBeanConfiguration`, `KafkaProducerConfiguration` |

### 2.3 A Regra de Dependência

Implementada e validada em tempo de build pelo `CleanArchitectureTest` com **ArchUnit**:

```
✅ PERMITIDO                          ❌ PROIBIDO
─────────────────────────────────    ─────────────────────────────────────
REST         → Application Service   domain-core importar Spring
REST         → Domain Core           domain-core importar JPA/Kafka
Application  → Domain Core           application-service importar infrastructure
Infrastructure → Application Service infrastructure importar REST
Container    → tudo                  application-service importar Spring directamente
```

A inversão de dependência é garantida pelos **Ports**:
- `OrderRepository` (interface em application-service) ← implementado por `OrderRepositoryImpl` (infrastructure)
- `OrderCreatedMessagePublisher` (interface em application-service) ← implementado por `OrderKafkaMessagePublisher` (infrastructure)

### 2.4 Fluxo SAGA Choreography

```
1. Cliente → POST /api/v1/orders {userId, productId}

2. order-service (Camada 2 — Use Case):
   • OrderCreateCommandHandler busca preço no catálogo
   • OrderDomainService.validateAndInitiateOrder() cria o aggregate Order
   • Persiste no banco (ordem: salvar DEPOIS validar)
   • Publica OrderCreatedEvent → saga-order-events

3. payment-service e inventory-service (em paralelo):
   • Ambos consomem OrderCreatedEvent de saga-order-events
   • payment-service: verifica/reserva saldo → publica PaymentEventMessage
   • inventory-service: verifica/reserva estoque → publica InventoryEventMessage

4. order-service (listener):
   • Consome PaymentEventMessage de saga-payment-events
   • Consome InventoryEventMessage de saga-inventory-events
   • OrderDomainService verifica ambos os status:
     - Ambos RESERVED  → Order.complete() → ORDER_COMPLETED
     - Qualquer REJECTED → Order.cancel() → ORDER_CANCELLED
     (Optional<Event> garante idempotência — sem publicação dupla)
```

---

## 3. Módulos Maven

```
saga-choreography-v2/                   ← Parent POM
│
├── common/
│   ├── common-domain/                  ← AggregateRoot<T>, BaseId<T>, Money, DomainEvent<T>
│   └── common-application/             ← Mensagens Kafka (OrderEventMessage, etc.), ApplicationDomainException
│
├── order-service/
│   ├── order-domain/
│   │   ├── order-domain-core/          ← Order, OrderDomainService, eventos
│   │   └── order-application-service/  ← Use cases, ports, DTOs, mapper
│   ├── order-infrastructure/           ← JPA, Kafka adapters
│   ├── order-application/              ← REST, Swagger
│   └── order-container/               ← Bootstrap, OpenAPI config, testes
│
├── payment-service/                    ← Mesma hierarquia (sem REST)
│   ├── payment-domain/
│   │   ├── payment-domain-core/        ← UserBalance, PaymentDomainService
│   │   └── payment-application-service/
│   ├── payment-infrastructure/
│   └── payment-container/
│
├── inventory-service/                  ← Mesma hierarquia (sem REST)
│   ├── inventory-domain/
│   │   ├── inventory-domain-core/      ← OrderInventory, InventoryDomainService
│   │   └── inventory-application-service/
│   ├── inventory-infrastructure/
│   └── inventory-container/
│
├── performance-tests/
│   ├── k6/                             ← smoke, load, stress, soak
│   └── jmeter/
├── k8s/                                ← Manifests Kubernetes (Kind)
├── docs/
│   ├── openapi/order-service-api.yaml  ← Especificação OpenAPI 3.1.0
│   └── postman/                        ← Collection completa (8 histórias de usuário)
└── docker-compose.yml
```

---

## 4. Stack Tecnológica

| Categoria | Tecnologia | Versão |
|-----------|-----------|--------|
| Linguagem | Java | 21 |
| Framework | Spring Boot | 3.3.5 |
| Mensageria | Apache Kafka (Confluent) | 7.5 |
| Persistência | PostgreSQL + Spring Data JPA + Flyway | 15 |
| Documentação API | springdoc-openapi (Swagger UI) | 2.6.0 |
| Rastreamento | Micrometer + Zipkin (Brave) | — |
| Métricas | Prometheus + Grafana | 2.47 / 10.2 |
| Logs | Logback + Logstash JSON encoder | 8.0 |
| Build | Maven (multi-módulo) | 3.9+ |
| Boilerplate | Lombok + MapStruct | 1.18.34 / 1.5.5 |
| Testes | JUnit 5 + Testcontainers + Cucumber + ArchUnit | 1.19.8 / 7.18 / 1.3 |
| Qualidade | Checkstyle (Google) + SpotBugs + PMD + JaCoCo | — |
| Performance | k6 + JMeter | — |
| Containers | Docker Compose + Kubernetes (Kind) | — |

---

## 5. Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker + Docker Compose
- (Opcional) kubectl + Kind para Kubernetes local
- (Opcional) k6 ou JMeter para testes de performance

---

## 6. Executando Localmente

### 6.1 Subir a infraestrutura

```bash
docker-compose up -d zookeeper kafka-broker kafka-ui \
  postgresql-order postgresql-payment postgresql-inventory \
  zipkin prometheus grafana
```

### 6.2 Compilar o projeto

```bash
mvn clean install -DskipTests
```

### 6.3 Executar os serviços individualmente

```bash
# Terminal 1 — Order Service (porta 8080)
cd order-service/order-container
mvn spring-boot:run

# Terminal 2 — Payment Service (porta 8081)
cd payment-service/payment-container
mvn spring-boot:run

# Terminal 3 — Inventory Service (porta 8082)
cd inventory-service/inventory-container
mvn spring-boot:run
```

### 6.4 Executar com Docker Compose (stack completa)

```bash
docker-compose up --build
```

---

## 7. Endpoints e Documentação API

### 7.1 REST Endpoints

Apenas o **order-service** expõe endpoints HTTP. Os demais comunicam-se exclusivamente via Kafka.

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| `POST` | `/api/v1/orders` | Criar pedido (inicia SAGA) | 201 / 400 |
| `GET` | `/api/v1/orders` | Listar todos os pedidos | 200 |

### 7.2 Documentação Interativa (Swagger UI)

Todos os três serviços expõem Swagger UI após inicialização:

| Serviço | Swagger UI | OpenAPI JSON | YAML estático |
|---------|-----------|--------------|---------------|
| order-service | http://localhost:8080/swagger-ui/index.html | http://localhost:8080/v3/api-docs | [`docs/openapi/order-service-api.yaml`](docs/openapi/order-service-api.yaml) |
| payment-service | http://localhost:8081/swagger-ui/index.html | http://localhost:8081/v3/api-docs | [`docs/openapi/payment-service-api.yaml`](docs/openapi/payment-service-api.yaml) |
| inventory-service | http://localhost:8082/swagger-ui/index.html | http://localhost:8082/v3/api-docs | [`docs/openapi/inventory-service-api.yaml`](docs/openapi/inventory-service-api.yaml) |

> **Nota:** payment-service e inventory-service não expõem endpoints de negócio. O Swagger UI
> documenta os endpoints de observabilidade (actuator) e os **contratos dos eventos Kafka**.

### 7.3 Postman Collection

Importe o arquivo [`docs/postman/saga-choreography-v2.postman_collection.json`](docs/postman/saga-choreography-v2.postman_collection.json) no Postman.

A collection contém **8 histórias de usuário** com testes automatizados:

| Pasta | História de Usuário |
|-------|---------------------|
| US-00 | Verificar saúde dos serviços |
| US-01 | Criar pedido com sucesso (Happy Path) |
| US-02 | Consultar todos os pedidos e verificar status da SAGA |
| US-03 | SAGA com pagamento rejeitado (saldo insuficiente) |
| US-04 | SAGA com estoque insuficiente |
| US-05 | Validação de entrada (campos inválidos/ausentes) |
| US-06 | Produto inexistente no catálogo |
| US-07 | Observabilidade — métricas e OpenAPI spec em runtime |
| US-08 | Resiliência — idempotência da SAGA |
| US-09 | Observabilidade de todos os serviços (payment + inventory) |

### 7.4 Exemplo de uso

```bash
# Criar pedido
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "productId": 1}'

# Aguardar ~2s e verificar status da SAGA
curl http://localhost:8080/api/v1/orders
```

**Dados de seed disponíveis:**

| Usuário | Saldo inicial |
|---------|--------------|
| 1 | R$ 1.000,00 |
| 2 | R$ 1.000,00 |
| 3 | R$ 1.000,00 |

| Produto | Preço | Estoque |
|---------|-------|---------|
| 1 | R$ 100,00 | 5 unid. |
| 2 | R$ 200,00 | 5 unid. |
| 3 | R$ 300,00 | 5 unid. |

---

## 8. Kafka — Tópicos e Mensagens

### 8.1 Tópicos

| Tópico | Publicado por | Consumido por |
|--------|--------------|---------------|
| `saga-order-events` | order-service | payment-service, inventory-service |
| `saga-payment-events` | payment-service | order-service |
| `saga-inventory-events` | inventory-service | order-service |

### 8.2 Mensagens de Evento

| Mensagem | Campos principais | Status possíveis |
|----------|------------------|-----------------|
| `OrderEventMessage` | orderId, userId, productId, price, orderStatus | `ORDER_CREATED`, `ORDER_COMPLETED`, `ORDER_CANCELLED` |
| `PaymentEventMessage` | paymentId, orderId, userId, amount, paymentStatus, failureMessages | `RESERVED`, `REJECTED` |
| `InventoryEventMessage` | inventoryId, orderId, productId, inventoryStatus, failureMessages | `RESERVED`, `REJECTED` |

### 8.3 Configuração de Produtores/Consumidores

| Configuração | Valor |
|---|---|
| Producer `acks` | `all` |
| Producer `retries` | `3` |
| Consumer `auto-offset-reset` | `earliest` |
| Trusted packages | `com.saga.common.application.events` |

---

## 9. Banco de Dados

Cada serviço tem seu próprio banco PostgreSQL — sem banco compartilhado (princípio de isolamento de dados por serviço).

| Serviço | Banco | Porta local | Tabelas principais |
|---------|-------|-------------|-------------------|
| order-service | `order_db` | 5432 | `orders` |
| payment-service | `payment_db` | 5433 | `user_balance`, `user_transaction` |
| inventory-service | `inventory_db` | 5434 | `order_inventory`, `order_inventory_consumption` |

**Credenciais locais:** `saga_user` / `saga_password`

Migrações gerenciadas pelo **Flyway** em `src/main/resources/db/migration/`. DDL automático desabilitado em produção (`ddl-auto: validate`).

---

## 10. Monitoramento e Observabilidade

| Interface | URL | Credenciais |
|-----------|-----|-------------|
| Kafka UI | http://localhost:8085 | — |
| Zipkin (tracing) | http://localhost:9411 | — |
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3000 | admin / admin |
| Actuator (order) | http://localhost:8080/actuator | — |
| Swagger UI (order) | http://localhost:8080/swagger-ui/index.html | — |

---

## 11. Testes

### 11.1 Executar todos os testes

```bash
mvn test
```

### 11.2 Por tipo

```bash
# Testes de domínio (sem Spring, sem banco)
mvn test -pl order-service/order-domain/order-domain-core

# Testes de integração (requer Docker para Testcontainers)
mvn test -pl order-service/order-container -Dtest="*IntegrationTest"

# Testes BDD Cucumber
mvn test -pl order-service/order-container -Dtest="CucumberRunner"

# Testes de arquitetura ArchUnit
mvn test -pl order-service/order-container -Dtest="CleanArchitectureTest"
```

### 11.3 Cobertura JaCoCo

```bash
mvn verify
# Relatório: {module}/target/site/jacoco/index.html
```

### 11.4 Estratégia de testes por camada

| Tipo | Módulo | Ferramenta | Requer infraestrutura? |
|------|--------|-----------|----------------------|
| Domínio | `*-domain-core` | JUnit 5 | Não |
| Arquitetura | `*-container` | ArchUnit | Não |
| BDD | `*-container` | Cucumber + Testcontainers | Sim (Docker) |
| Integração | `*-container` | @SpringBootTest + Testcontainers | Sim (Docker) |

---

## 12. Testes de Performance

### k6

```bash
# Smoke test (carga mínima — sanidade)
k6 run performance-tests/k6/smoke-test.js

# Load test (carga esperada em produção)
k6 run performance-tests/k6/load-test.js

# Stress test (acima do limite)
k6 run performance-tests/k6/stress-test.js

# Soak test (carga sustentada por longo período)
k6 run performance-tests/k6/soak-test.js
```

### JMeter

```bash
jmeter -n -t performance-tests/jmeter/order-service-load-test.jmx \
       -l results.jtl -e -o report/
```

---

## 13. Kubernetes

```bash
# Criar cluster Kind local
kind create cluster --config k8s/kind-cluster.yaml

# Deploy completo via script
bash k8s/deploy.sh

# Ou manualmente
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/kafka/
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/services/
kubectl apply -f k8s/monitoring/
```

---

## 14. Qualidade de Código

Plugins Maven ativos em todos os módulos:

```bash
mvn checkstyle:check    # Google Java Style
mvn spotbugs:check      # Análise estática de bugs
mvn pmd:check           # Regras de qualidade (ruleset customizado)
mvn verify              # Tudo + JaCoCo (mínimo 80% nas camadas domain e application-service)
```

---

## 15. Variáveis de Ambiente

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `SPRING_DATASOURCE_URL` | URL JDBC do banco | `jdbc:postgresql://localhost:5432/order_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `saga_user` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `saga_password` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Endereço do broker Kafka | `kafka-broker:29092` |
| `SPRING_PROFILES_ACTIVE` | Perfil Spring ativo | `docker` |

**Perfis disponíveis:**

| Perfil | Uso | Fonte de configuração |
|--------|-----|-----------------------|
| `default` | Desenvolvimento local | `application.yml` |
| `docker` | Containers Docker | `application-docker.yml` |
| `test` | Testes de integração | `application-test.yml` |

---

## 16. Decisões de Design

**Por que Choreography e não Orchestration?**
Cada serviço é totalmente autônomo — não há ponto único de falha nem acoplamento centralizado. Novos serviços participam da SAGA apenas assinando os tópicos relevantes, sem alterar nenhum orquestrador.

**Por que Clean Architecture (Uncle Bob)?**
O domínio de negócio é isolado de frameworks, banco de dados e mensageria. É possível testar toda a lógica de negócio sem levantar Spring, Kafka ou PostgreSQL. A dependência sempre aponta para o centro, garantindo que o núcleo nunca "conhece" a infraestrutura.

**Por que um banco por serviço?**
Garante isolamento de dados e autonomia de deploy. Cada serviço evolui seu schema de forma independente sem risco de acoplamento via banco compartilhado.

**Por que `Optional<Event>` nos domain services?**
Idempotência explícita: se o aggregate já está no estado alvo (ex: pedido já cancelado por pagamento rejeitado), o domain service retorna `Optional.empty()` e nenhum evento duplicado é publicado ao processar a resposta do inventário.

**Por que `@Version` nas JPA entities?**
Optimistic locking garante que atualizações concorrentes da SAGA (pagamento e inventário chegando ao mesmo tempo) não causem sobrescrita silenciosa de dados.

**Por que InMemoryProductCatalogRepository?**
Simplificação proposital para o exemplo. Em produção, isso seria um serviço de catálogo separado (ou uma chamada read-through com cache). A interface `ProductCatalogRepository` já está definida como Port, facilitando a substituição.
