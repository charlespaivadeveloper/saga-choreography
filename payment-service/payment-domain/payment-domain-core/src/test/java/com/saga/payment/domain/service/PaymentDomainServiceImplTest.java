package com.saga.payment.domain.service;

import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.UserId;
import com.saga.payment.domain.entity.UserBalance;
import com.saga.payment.domain.entity.UserTransaction;
import com.saga.payment.domain.event.PaymentRejectedEvent;
import com.saga.payment.domain.event.PaymentReservedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

@DisplayName("PaymentDomainService")
class PaymentDomainServiceImplTest {

    private PaymentDomainService paymentDomainService;

    private OrderId orderId;
    private UserId userId;

    @BeforeEach
    void setUp() {
        paymentDomainService = new PaymentDomainServiceImpl();
        orderId = OrderId.of(UUID.randomUUID());
        userId = new UserId(1L);
    }

    @Nested
    @DisplayName("reservePayment")
    class ReservePayment {

        @Test
        @DisplayName("returns PaymentReservedEvent when balance is sufficient")
        void returnsReservedEventWhenBalanceSufficient() {
            // Given
            var balance = UserBalance.builder()
                .userId(userId)
                .balance(new Money(new BigDecimal("500.00")))
                .build();
            var amount = new Money(new BigDecimal("100.00"));

            // When
            PaymentReservedEvent result = paymentDomainService.reservePayment(orderId, userId, amount, balance);

            // Then
            then(result).isNotNull();
            then(result.getOrderId()).isEqualTo(orderId);
            then(result.getUserId()).isEqualTo(userId);
            then(result.getAmount()).isEqualTo(amount);
            then(result.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("deducts amount from balance when reservation succeeds")
        void deductsAmountFromBalanceOnSuccess() {
            // Given
            var initialBalance = new Money(new BigDecimal("1000.00"));
            var balance = UserBalance.builder()
                .userId(userId)
                .balance(initialBalance)
                .build();
            var amount = new Money(new BigDecimal("300.00"));

            // When
            paymentDomainService.reservePayment(orderId, userId, amount, balance);

            // Then
            then(balance.getBalance()).isEqualTo(new Money(new BigDecimal("700.00")));
        }

        @Test
        @DisplayName("returns null when balance is insufficient")
        void returnsNullWhenBalanceInsufficient() {
            // Given
            var balance = UserBalance.builder()
                .userId(userId)
                .balance(new Money(new BigDecimal("50.00")))
                .build();
            var amount = new Money(new BigDecimal("100.00"));

            // When
            PaymentReservedEvent result = paymentDomainService.reservePayment(orderId, userId, amount, balance);

            // Then
            then(result).isNull();
        }

        @Test
        @DisplayName("does not deduct from balance when reservation fails")
        void doesNotDeductBalanceOnFailure() {
            // Given
            var initialBalance = new Money(new BigDecimal("50.00"));
            var balance = UserBalance.builder()
                .userId(userId)
                .balance(initialBalance)
                .build();
            var amount = new Money(new BigDecimal("100.00"));

            // When
            paymentDomainService.reservePayment(orderId, userId, amount, balance);

            // Then
            then(balance.getBalance()).isEqualTo(initialBalance);
        }

        @Test
        @DisplayName("succeeds when amount exactly equals balance")
        void succeedsWhenAmountEqualsBalance() {
            // Given
            var exactBalance = new Money(new BigDecimal("100.00"));
            var balance = UserBalance.builder()
                .userId(userId)
                .balance(exactBalance)
                .build();
            var amount = new Money(new BigDecimal("100.00"));

            // When
            PaymentReservedEvent result = paymentDomainService.reservePayment(orderId, userId, amount, balance);

            // Then
            then(result).isNotNull();
            then(balance.getBalance()).isEqualTo(new Money(BigDecimal.ZERO.setScale(2)));
        }
    }

    @Nested
    @DisplayName("rejectPayment")
    class RejectPayment {

        @Test
        @DisplayName("returns PaymentRejectedEvent with failure message")
        void returnsRejectedEventWithFailureMessage() {
            // Given
            var balance = UserBalance.builder()
                .userId(userId)
                .balance(new Money(new BigDecimal("700.00")))
                .build();
            var refundAmount = new Money(new BigDecimal("300.00"));
            var userTransaction = UserTransaction.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(refundAmount)
                .build();

            // When
            PaymentRejectedEvent result = paymentDomainService.rejectPayment(orderId, userId, balance, userTransaction);

            // Then
            then(result).isNotNull();
            then(result.getOrderId()).isEqualTo(orderId);
            then(result.getUserId()).isEqualTo(userId);
            then(result.getFailureMessages()).isNotEmpty();
            then(result.getFailureMessages().get(0)).contains("refunded");
        }

        @Test
        @DisplayName("refunds amount back to balance")
        void refundsAmountBackToBalance() {
            // Given
            var currentBalance = new Money(new BigDecimal("700.00"));
            var balance = UserBalance.builder()
                .userId(userId)
                .balance(currentBalance)
                .build();
            var refundAmount = new Money(new BigDecimal("300.00"));
            var userTransaction = UserTransaction.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(refundAmount)
                .build();

            // When
            paymentDomainService.rejectPayment(orderId, userId, balance, userTransaction);

            // Then
            then(balance.getBalance()).isEqualTo(new Money(new BigDecimal("1000.00")));
        }

        @Test
        @DisplayName("does not throw when processing valid refund")
        void doesNotThrowOnValidRefund() {
            // Given
            var balance = UserBalance.builder()
                .userId(userId)
                .balance(new Money(new BigDecimal("0.00")))
                .build();
            var userTransaction = UserTransaction.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(new Money(new BigDecimal("100.00")))
                .build();

            // When / Then
            thenNoException().isThrownBy(() ->
                paymentDomainService.rejectPayment(orderId, userId, balance, userTransaction));
        }
    }
}
