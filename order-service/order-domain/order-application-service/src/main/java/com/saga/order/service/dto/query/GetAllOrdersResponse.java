package com.saga.order.service.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Resposta contendo a lista de todos os pedidos registrados.")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllOrdersResponse {

    @Schema(description = "Lista de pedidos. Pode ser vazia se nenhum pedido foi criado.")
    private List<OrderDto> orders;
}
