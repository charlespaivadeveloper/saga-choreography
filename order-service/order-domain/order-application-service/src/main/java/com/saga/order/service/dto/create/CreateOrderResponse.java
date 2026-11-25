package com.saga.order.service.dto.create;

import com.saga.common.domain.valueobject.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Schema(description = "Resposta imediata após criação do pedido. A SAGA ainda está em andamento.")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {

    @Schema(
        description = "UUID gerado para o pedido. Use para rastrear o status via GET /api/v1/orders.",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID orderId;

    @Schema(
        description = "Status inicial do pedido. Sempre ORDER_CREATED neste momento.",
        example = "ORDER_CREATED"
    )
    private OrderStatus orderStatus;

    @Schema(
        description = "Mensagem descritiva do resultado",
        example = "Order created successfully"
    )
    private String message;
}
