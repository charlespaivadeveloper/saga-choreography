package com.saga.order.service.dto.query;

import com.saga.common.domain.valueobject.InventoryStatus;
import com.saga.common.domain.valueobject.OrderStatus;
import com.saga.common.domain.valueobject.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private UUID orderId;
    private Long userId;
    private Long productId;
    private BigDecimal price;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private InventoryStatus inventoryStatus;
    private List<String> failureMessages;
}
