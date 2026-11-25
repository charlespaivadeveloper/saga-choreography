package com.saga.order.service.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Comando para criação de um novo pedido")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand {

    @Schema(
        description = "ID do usuário. Deve ter saldo cadastrado no payment-service. Usuários seed: 1, 2, 3.",
        example = "1",
        minimum = "1"
    )
    @NotNull(message = "userId is required")
    @Positive(message = "userId must be positive")
    private Long userId;

    @Schema(
        description = "ID do produto no catálogo interno. Produtos seed: 1 (R$ 100), 2 (R$ 200), 3 (R$ 300).",
        example = "1",
        minimum = "1"
    )
    @NotNull(message = "productId is required")
    @Positive(message = "productId must be positive")
    private Long productId;
}
