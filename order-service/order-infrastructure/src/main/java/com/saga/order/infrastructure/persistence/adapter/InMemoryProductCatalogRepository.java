package com.saga.order.infrastructure.persistence.adapter;

import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.order.service.ports.output.repository.ProductCatalogRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryProductCatalogRepository implements ProductCatalogRepository {

    private static final Map<Long, BigDecimal> PRODUCT_PRICES = Map.of(
        1L, new BigDecimal("100.00"),
        2L, new BigDecimal("200.00"),
        3L, new BigDecimal("300.00")
    );

    @Override
    public Optional<Money> findPriceByProductId(ProductId productId) {
        return Optional.ofNullable(PRODUCT_PRICES.get(productId.getValue()))
            .map(Money::of);
    }
}
