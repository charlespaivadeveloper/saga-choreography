package com.saga.inventory.config;

import com.saga.inventory.domain.service.InventoryDomainService;
import com.saga.inventory.domain.service.InventoryDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryBeanConfiguration {

    @Bean
    public InventoryDomainService inventoryDomainService() {
        return new InventoryDomainServiceImpl();
    }
}
