package com.saga.payment.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "Payment Service — Observability API",
        version = "1.0.0",
        description = """
            Endpoints de observabilidade do **Payment Service** no sistema **SAGA Choreography v2**.

            Este serviço **não expõe endpoints de negócio via HTTP**. Toda a comunicação
            de negócio ocorre via eventos Kafka.

            ## Responsabilidade no sistema

            O Payment Service processa pedidos de reserva e cancelamento de pagamento:

            | Evento consumido (saga-order-events) | Ação | Evento publicado (saga-payment-events) |
            |--------------------------------------|------|---------------------------------------|
            | `ORDER_CREATED` | Verifica saldo de `user_balance`, reserva valor, registra em `user_transaction` | `PaymentEventMessage(RESERVED)` |
            | `ORDER_CANCELLED` | Verifica se há transação ativa, devolve valor ao saldo | `PaymentEventMessage(REJECTED)` ou sem evento |

            ## Arquitetura — Camadas (Uncle Bob)

            | Módulo | Camada |
            |--------|--------|
            | `payment-domain-core` | Entities — `UserBalance`, `UserTransaction`, `PaymentDomainService` |
            | `payment-application-service` | Use Cases — `OrderEventMessageListenerImpl`, output ports |
            | `payment-infrastructure` | Interface Adapters — JPA adapters, Kafka publishers/listeners |
            | `payment-container` | Frameworks & Drivers — Spring Boot bootstrap |

            ## Dados de seed

            | user_id | balance |
            |---------|---------|
            | 1 | R$ 1.000,00 |
            | 2 | R$ 1.000,00 |
            | 3 | R$ 1.000,00 |
            """,
        contact = @Contact(name = "SAGA Choreography Team", email = "paiva.charles@gmail.com"),
        license = @License(name = "MIT")
    ),
    servers = {
        @Server(url = "http://localhost:8081", description = "Desenvolvimento local"),
        @Server(url = "http://payment-service:8081", description = "Docker Compose")
    }
)
@Configuration
public class OpenApiConfiguration {
}
