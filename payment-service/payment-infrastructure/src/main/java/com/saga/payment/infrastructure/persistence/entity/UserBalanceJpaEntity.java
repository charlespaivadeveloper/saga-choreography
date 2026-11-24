package com.saga.payment.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_balance")
public class UserBalanceJpaEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @Version
    private Long version;
}
