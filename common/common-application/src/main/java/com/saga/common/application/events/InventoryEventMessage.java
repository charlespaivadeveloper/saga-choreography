package com.saga.common.application.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEventMessage {

    @JsonProperty("inventory_id")
    private String inventoryId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("inventory_status")
    private String inventoryStatus;

    @JsonProperty("failure_messages")
    private List<String> failureMessages;

    @JsonProperty("created_at")
    private Instant createdAt;
}
