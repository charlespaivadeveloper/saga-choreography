package com.saga.inventory.infrastructure.persistence.entity;

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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_inventory")
public class OrderInventoryJpaEntity {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "available_inventory", nullable = false)
    private int availableInventory;

    @Version
    private Long version;
}
