package com.saga.inventory.service;

import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.Product;
import com.saga.inventory.domain.exception.ProductNotFoundException;
import com.saga.inventory.service.dto.query.ProductDto;
import com.saga.inventory.service.mapper.InventoryDataMapper;
import com.saga.inventory.service.ports.input.service.ProductCatalogApplicationService;
import com.saga.inventory.service.ports.output.repository.OrderInventoryRepository;
import com.saga.inventory.service.ports.output.repository.ProductRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Observed(name = "saga.inventory.catalog", contextualName = "inventory-catalog")
public class ProductCatalogApplicationServiceImpl implements ProductCatalogApplicationService {

    private final ProductRepository productRepository;
    private final OrderInventoryRepository orderInventoryRepository;
    private final InventoryDataMapper inventoryDataMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAllActive().stream()
                .map(this::enrichWithStock)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long productId) {
        Product product = productRepository.findById(new ProductId(productId))
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with id: " + productId));
        return enrichWithStock(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategory(String category) {
        return productRepository.findByCategoryActive(category.toUpperCase()).stream()
                .map(this::enrichWithStock)
                .toList();
    }

    private ProductDto enrichWithStock(Product product) {
        int qty = orderInventoryRepository.findByProductId(product.getId())
                .map(inv -> inv.getAvailableQuantity())
                .orElse(0);
        return inventoryDataMapper.toProductDto(product, qty);
    }
}
