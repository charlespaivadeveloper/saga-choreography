package com.saga.order.service.dto.query;

import com.saga.common.domain.valueobject.InventoryStatus;
import com.saga.common.domain.valueobject.OrderStatus;
import com.saga.common.domain.valueobject.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Representação completa de um pedido com estado atual da SAGA.")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    @Schema(description = "UUID do pedido", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID orderId;

    @Schema(description = "ID do usuário que criou o pedido", example = "1")
    private Long userId;

    @Schema(description = "ID do produto comprado", example = "1")
    private Long productId;

    @Schema(description = "Preço do produto no momento da criação", example = "100.00")
    private BigDecimal price;

    @Schema(
        description = """
            Estado do ciclo de vida do pedido:
            - ORDER_CREATED: SAGA em andamento (transitório)
            - ORDER_COMPLETED: pagamento e estoque reservados (sucesso)
            - ORDER_CANCELLED: pagamento ou estoque rejeitado (falha)
            """,
        example = "ORDER_COMPLETED"
    )
    private OrderStatus orderStatus;

    @Schema(
        description = "Estado do pagamento. null = aguardando. RESERVED = ok. REJECTED = saldo insuficiente.",
        nullable = true,
        example = "RESERVED"
    )
    private PaymentStatus paymentStatus;

    @Schema(
        description = "Estado do estoque. null = aguardando. RESERVED = ok. REJECTED = estoque insuficiente.",
        nullable = true,
        example = "RESERVED"
    )
    private InventoryStatus inventoryStatus;

    @Schema(
        description = "Mensagens de falha da SAGA. Vazio em caso de sucesso.",
        example = "[]"
    )
    private List<String> failureMessages;
}
