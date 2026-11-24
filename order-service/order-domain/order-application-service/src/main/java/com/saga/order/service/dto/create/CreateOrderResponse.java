package com.saga.order.service.dto.create;

import com.saga.common.domain.valueobject.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {

    private UUID orderId;
    private OrderStatus orderStatus;
    private String message;
}
