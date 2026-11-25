package com.saga.inventory.service.dto.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
public class ProductDto {

    @JsonProperty("product_id")
    private final Long productId;

    private final String sku;
    private final String name;
    private final String description;
    private final String category;
    private final BigDecimal price;

    @JsonProperty("weight_grams")
    private final Integer weightGrams;

    private final Map<String, Object> attributes;
    private final boolean active;

    @JsonProperty("available_quantity")
    private final int availableQuantity;
}
