package com.saga.common.application.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventMessage {

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("order_status")
    private String orderStatus;

    @JsonProperty("saga_id")
    private String sagaId;

    @JsonProperty("created_at")
    private Instant createdAt;
}
