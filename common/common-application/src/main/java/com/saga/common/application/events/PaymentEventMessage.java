package com.saga.common.application.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventMessage {

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("failure_messages")
    private List<String> failureMessages;

    @JsonProperty("created_at")
    private Instant createdAt;
}
