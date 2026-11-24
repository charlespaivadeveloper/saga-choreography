package com.saga.order.service.ports.output.repository;

import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.ProductId;

import java.util.Optional;

public interface ProductCatalogRepository {

    Optional<Money> findPriceByProductId(ProductId productId);
}
