package com.saga.inventory.infrastructure.persistence.adapter;

import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.Product;
import com.saga.inventory.infrastructure.persistence.mapper.ProductPersistenceMapper;
import com.saga.inventory.infrastructure.persistence.repository.ProductJpaRepository;
import com.saga.inventory.service.ports.output.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final ProductPersistenceMapper productPersistenceMapper;

    @Override
    public List<Product> findAllActive() {
        return productJpaRepository.findByActiveTrue().stream()
                .map(productPersistenceMapper::toDomainEntity)
                .toList();
    }

    @Override
    public Optional<Product> findById(ProductId productId) {
        return productJpaRepository.findById(productId.getValue())
                .map(productPersistenceMapper::toDomainEntity);
    }

    @Override
    public List<Product> findByCategoryActive(String category) {
        return productJpaRepository.findByCategoryAndActiveTrue(category).stream()
                .map(productPersistenceMapper::toDomainEntity)
                .toList();
    }
}
