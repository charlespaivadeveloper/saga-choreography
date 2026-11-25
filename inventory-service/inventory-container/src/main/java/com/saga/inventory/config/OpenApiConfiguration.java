package com.saga.inventory.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "Inventory Service — Observability API",
        version = "1.0.0",
        description = """
            Endpoints de observabilidade do **Inventory Service** no sistema **SAGA Choreography v2**.

            Este serviço **não expõe endpoints de negócio via HTTP**. Toda a comunicação
            de negócio ocorre via eventos Kafka.

            ## Responsabilidade no sistema

            O Inventory Service processa pedidos de reserva e devolução de estoque:

            | Evento consumido (saga-order-events) | Ação | Evento publicado (saga-inventory-events) |
            |--------------------------------------|------|------------------------------------------|
            | `ORDER_CREATED` | Verifica `order_inventory`, decrementa `available_inventory`, registra em `order_inventory_consumption` | `InventoryEventMessage(RESERVED)` |
            | `ORDER_CANCELLED` | Remove o registro de consumo e devolve unidade ao estoque | `InventoryEventMessage(REJECTED)` ou sem evento |

            ## Arquitetura — Camadas (Uncle Bob)

            | Módulo | Camada |
            |--------|--------|
            | `inventory-domain-core` | Entities — `OrderInventory`, `OrderInventoryConsumption`, `InventoryDomainService` |
            | `inventory-application-service` | Use Cases — `OrderEventMessageListenerImpl`, output ports |
            | `inventory-infrastructure` | Interface Adapters — JPA adapters, Kafka publishers/listeners |
            | `inventory-container` | Frameworks & Drivers — Spring Boot bootstrap |

            ## Dados de seed

            | product_id | available_inventory |
            |------------|---------------------|
            | 1 | 5 |
            | 2 | 5 |
            | 3 | 5 |
            """,
        contact = @Contact(name = "SAGA Choreography Team", email = "paiva.charles@gmail.com"),
        license = @License(name = "MIT")
    ),
    servers = {
        @Server(url = "http://localhost:8082", description = "Desenvolvimento local"),
        @Server(url = "http://inventory-service:8082", description = "Docker Compose")
    }
)
@Configuration
public class OpenApiConfiguration {
}
