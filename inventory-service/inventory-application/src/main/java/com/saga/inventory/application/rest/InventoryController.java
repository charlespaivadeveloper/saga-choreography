package com.saga.inventory.application.rest;

import com.saga.inventory.service.dto.query.ProductDto;
import com.saga.inventory.service.ports.input.service.ProductCatalogApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Catalog", description = "Product catalog with real-time stock availability")
public class InventoryController {

    private final ProductCatalogApplicationService productCatalogApplicationService;

    @GetMapping
    @Operation(summary = "List all active products with stock availability")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productCatalogApplicationService.getAllProducts());
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product details and stock by ID")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productCatalogApplicationService.getProductById(productId));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Filter active products by category (e.g. ELECTRONICS, ACCESSORIES)")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productCatalogApplicationService.getProductsByCategory(category));
    }
}
