package com.saga.order.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "Order Service API",
        version = "1.0.0",
        description = """
            API REST do **Order Service** no sistema **SAGA Choreography v2**.

            Este serviço é o único ponto de entrada HTTP do sistema.
            `payment-service` e `inventory-service` comunicam-se exclusivamente via Kafka.

            ## Arquitetura
            Implementa Clean Architecture (Uncle Bob) com quatro camadas:
            - **Entities** → `order-domain-core` (Order, eventos de domínio, domain service)
            - **Use Cases** → `order-application-service` (command handlers, ports)
            - **Interface Adapters** → `order-application` (REST) + `order-infrastructure` (JPA/Kafka)
            - **Frameworks & Drivers** → `order-container` (Spring Boot bootstrap)

            ## Dados de seed
            **Usuários:** 1, 2, 3 (saldo: R$ 1.000,00 cada)

            **Produtos:** 1 → R$ 100 | 2 → R$ 200 | 3 → R$ 300 (estoque: 5 unid. cada)
            """,
        contact = @Contact(name = "SAGA Choreography Team", email = "paiva.charles@gmail.com"),
        license = @License(name = "MIT")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Desenvolvimento local"),
        @Server(url = "http://order-service:8080", description = "Docker Compose")
    }
)
@Configuration
public class OpenApiConfiguration {
}
