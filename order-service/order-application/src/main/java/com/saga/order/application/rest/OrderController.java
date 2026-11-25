package com.saga.order.application.rest;

import com.saga.order.service.dto.create.CreateOrderCommand;
import com.saga.order.service.dto.create.CreateOrderResponse;
import com.saga.order.service.dto.query.GetAllOrdersResponse;
import com.saga.order.service.ports.input.service.OrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Tag(name = "orders", description = "Gerenciamento de pedidos. Único ponto de entrada HTTP do sistema SAGA.")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderApplicationService orderApplicationService;

    @Operation(
        summary = "Criar um novo pedido",
        description = """
            Inicia o fluxo SAGA criando um pedido. O pedido é persistido imediatamente
            com status `ORDER_CREATED` e um evento é publicado em `saga-order-events`.

            O status final é definido **assincronamente**:
            - `ORDER_COMPLETED` → pagamento e estoque reservados com sucesso
            - `ORDER_CANCELLED` → pagamento ou estoque rejeitado (transações compensadas)

            **Produtos disponíveis (seed):** 1 (R$ 100), 2 (R$ 200), 3 (R$ 300)

            **Usuários disponíveis (seed):** 1, 2, 3 (cada um com R$ 1.000,00 de saldo)
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Pedido criado — SAGA iniciada (status final virá via Kafka)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CreateOrderResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "orderId": "550e8400-e29b-41d4-a716-446655440000",
                          "orderStatus": "ORDER_CREATED",
                          "message": "Order created successfully"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida — validação ou regra de negócio violada",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "validation_error",
                        summary = "Campo obrigatório ausente",
                        value = """
                            {
                              "code": "VALIDATION_ERROR",
                              "message": "userId: userId is required",
                              "timestamp": "2024-11-25T10:30:00Z"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "domain_error",
                        summary = "Produto não encontrado no catálogo",
                        value = """
                            {
                              "code": "ORDER_DOMAIN_ERROR",
                              "message": "Product not found in catalog: 999",
                              "timestamp": "2024-11-25T10:30:00Z"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)
            )
        )
    })
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderCommand createOrderCommand) {
        log.info("Creating order for userId: {} productId: {}",
            createOrderCommand.getUserId(), createOrderCommand.getProductId());
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderCommand);
        log.info("Order created with id: {}", response.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Listar todos os pedidos",
        description = """
            Retorna todos os pedidos registrados no banco de dados local do order-service,
            incluindo o status final da SAGA e os status de pagamento e inventário.

            Consulte este endpoint após ~2s de um POST para observar o resultado assíncrono da SAGA.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de pedidos (pode ser vazia)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GetAllOrdersResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<GetAllOrdersResponse> getAllOrders() {
        GetAllOrdersResponse response = orderApplicationService.getAllOrders();
        return ResponseEntity.ok(response);
    }
}
