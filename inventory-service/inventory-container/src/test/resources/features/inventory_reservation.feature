Feature: Inventory Reservation
  As the inventory service
  I want to process inventory reservation events from the SAGA
  So that I can reserve or reject inventory for an order

  Scenario: Successfully reserve inventory for a product with available stock
    Given a customer with userId 1 wants to reserve product 1
    When the inventory reservation is processed
    Then the inventory reservation should be processed without errors

  Scenario: Successfully reserve inventory for a different product
    Given a customer with userId 2 wants to reserve product 2
    When the inventory reservation is processed
    Then the inventory reservation should be processed without errors

  Scenario: Fail to reserve inventory for a non-existent product
    Given a customer with userId 1 wants to reserve product 999
    When the inventory reservation is processed
    Then the inventory reservation should fail with error containing "Product not found"

  Scenario: Verify inventory exists for seeded product
    Given a customer with userId 1 wants to reserve product 1
    Then the inventory for productId 1 should exist

  Scenario: Verify inventory has available stock for seeded product
    Given a customer with userId 1 wants to reserve product 1
    Then the inventory for productId 1 should have available stock
