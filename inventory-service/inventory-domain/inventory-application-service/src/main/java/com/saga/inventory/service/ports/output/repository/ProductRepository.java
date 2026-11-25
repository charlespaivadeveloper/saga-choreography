package com.saga.inventory.service.ports.output.repository;

import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<Product> findAllActive();

    Optional<Product> findById(ProductId productId);

    List<Product> findByCategoryActive(String category);
}
