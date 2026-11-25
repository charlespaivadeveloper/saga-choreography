package com.saga.inventory.service.mapper;

import com.saga.inventory.domain.entity.Product;
import com.saga.inventory.service.dto.query.ProductDto;
import org.springframework.stereotype.Component;

@Component
public class InventoryDataMapper {

    public ProductDto toProductDto(Product product, int availableQuantity) {
        return ProductDto.builder()
                .productId(product.getId().getValue())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory().name())
                .price(product.getPrice().getAmount())
                .weightGrams(product.getWeightGrams())
                .attributes(product.getAttributes())
                .active(product.isActive())
                .availableQuantity(availableQuantity)
                .build();
    }
}
