package com.saga.inventory.infrastructure.persistence.repository;

import com.saga.inventory.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    List<ProductJpaEntity> findByActiveTrue();

    List<ProductJpaEntity> findByCategoryAndActiveTrue(String category);
}
