package com.saga.inventory.infrastructure.persistence.mapper;

import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.Product;
import com.saga.inventory.domain.valueobject.ProductCategory;
import com.saga.inventory.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductPersistenceMapper {

    public Product toDomainEntity(ProductJpaEntity entity) {
        return Product.builder()
                .productId(new ProductId(entity.getId()))
                .sku(entity.getSku())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(ProductCategory.valueOf(entity.getCategory()))
                .price(new Money(entity.getPrice()))
                .weightGrams(entity.getWeightGrams())
                .attributes(entity.getAttributes())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
