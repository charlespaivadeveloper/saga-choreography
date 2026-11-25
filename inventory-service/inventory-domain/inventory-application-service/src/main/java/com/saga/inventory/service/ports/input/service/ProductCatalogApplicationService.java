package com.saga.inventory.service.ports.input.service;

import com.saga.inventory.service.dto.query.ProductDto;

import java.util.List;

public interface ProductCatalogApplicationService {

    List<ProductDto> getAllProducts();

    ProductDto getProductById(Long productId);

    List<ProductDto> getProductsByCategory(String category);
}
