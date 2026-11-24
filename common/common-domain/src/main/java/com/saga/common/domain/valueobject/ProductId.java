package com.saga.common.domain.valueobject;

public class ProductId extends BaseId<Long> {

    public ProductId(Long value) {
        super(value);
    }

    public static ProductId of(Long value) {
        return new ProductId(value);
    }
}
