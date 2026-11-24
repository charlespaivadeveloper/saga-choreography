package com.saga.common.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.BDDAssertions.then;

@DisplayName("Money Value Object")
class MoneyTest {

    @Test
    @DisplayName("should add two money amounts correctly")
    void shouldAddTwoMoneyAmountsCorrectly() {
        // Given
        Money money1 = Money.of("100.00");
        Money money2 = Money.of("50.00");

        // When
        Money result = money1.add(money2);

        // Then
        then(result.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("should subtract two money amounts correctly")
    void shouldSubtractTwoMoneyAmountsCorrectly() {
        // Given
        Money money1 = Money.of("100.00");
        Money money2 = Money.of("30.00");

        // When
        Money result = money1.subtract(money2);

        // Then
        then(result.getAmount()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    @DisplayName("should multiply money by integer correctly")
    void shouldMultiplyMoneyByIntegerCorrectly() {
        // Given
        Money money = Money.of("25.00");

        // When
        Money result = money.multiply(4);

        // Then
        then(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("should return true when money is greater than zero")
    void shouldReturnTrueWhenMoneyIsGreaterThanZero() {
        // Given
        Money positive = Money.of("1.00");

        // When & Then
        then(positive.isGreaterThanZero()).isTrue();
        then(Money.ZERO.isGreaterThanZero()).isFalse();
    }

    @Test
    @DisplayName("should compare two money amounts correctly")
    void shouldCompareTwoMoneyAmountsCorrectly() {
        // Given
        Money bigger = Money.of("200.00");
        Money smaller = Money.of("100.00");

        // When & Then
        then(bigger.isGreaterThan(smaller)).isTrue();
        then(smaller.isGreaterThan(bigger)).isFalse();
        then(bigger.isGreaterThanOrEqualTo(bigger)).isTrue();
        then(smaller.isGreaterThanOrEqualTo(smaller)).isTrue();
    }

    @Test
    @DisplayName("should consider two money objects with same amount equal")
    void shouldConsiderTwoMoneyObjectsWithSameAmountEqual() {
        // Given
        Money money1 = Money.of("100.00");
        Money money2 = Money.of("100.00");

        // When & Then
        then(money1).isEqualTo(money2);
        then(money1.hashCode()).isEqualTo(money2.hashCode());
    }

    @Test
    @DisplayName("should return string representation of amount")
    void shouldReturnStringRepresentationOfAmount() {
        // Given
        Money money = Money.of("99.99");

        // When
        String result = money.toString();

        // Then
        then(result).isEqualTo("99.99");
    }
}
