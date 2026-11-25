package com.saga.inventory.domain.entity;

import com.saga.common.domain.entity.AggregateRoot;
import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.valueobject.ProductCategory;

import java.time.Instant;
import java.util.Map;

public class Product extends AggregateRoot<ProductId> {

    private final String sku;
    private final String name;
    private final String description;
    private final ProductCategory category;
    private final Money price;
    private final Integer weightGrams;
    private final Map<String, Object> attributes;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Product(Builder builder) {
        setId(builder.productId);
        this.sku = builder.sku;
        this.name = builder.name;
        this.description = builder.description;
        this.category = builder.category;
        this.price = builder.price;
        this.weightGrams = builder.weightGrams;
        this.attributes = builder.attributes;
        this.active = builder.active;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public String getSku()                    { return sku; }
    public String getName()                   { return name; }
    public String getDescription()            { return description; }
    public ProductCategory getCategory()      { return category; }
    public Money getPrice()                   { return price; }
    public Integer getWeightGrams()           { return weightGrams; }
    public Map<String, Object> getAttributes(){ return attributes; }
    public boolean isActive()                 { return active; }
    public Instant getCreatedAt()             { return createdAt; }
    public Instant getUpdatedAt()             { return updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ProductId productId;
        private String sku;
        private String name;
        private String description;
        private ProductCategory category;
        private Money price;
        private Integer weightGrams;
        private Map<String, Object> attributes;
        private boolean active;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {}

        public Builder productId(ProductId val)              { productId = val;    return this; }
        public Builder sku(String val)                       { sku = val;          return this; }
        public Builder name(String val)                      { name = val;         return this; }
        public Builder description(String val)               { description = val;  return this; }
        public Builder category(ProductCategory val)         { category = val;     return this; }
        public Builder price(Money val)                      { price = val;        return this; }
        public Builder weightGrams(Integer val)              { weightGrams = val;  return this; }
        public Builder attributes(Map<String, Object> val)   { attributes = val;   return this; }
        public Builder active(boolean val)                   { active = val;       return this; }
        public Builder createdAt(Instant val)                { createdAt = val;    return this; }
        public Builder updatedAt(Instant val)                { updatedAt = val;    return this; }

        public Product build() {
            return new Product(this);
        }
    }
}
