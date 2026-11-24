Feature: Order Creation
  As a customer
  I want to create an order for a product
  So that the saga can coordinate payment and inventory reservation

  Background:
    Given a customer with userId 1 wants to buy product 1

  Scenario: Successfully create an order
    When the customer creates an order
    Then the order should be created successfully
    And the order response message should be "Order created successfully"

  Scenario: Create order for different products
    Given a customer with userId 2 wants to buy product 2
    When the customer creates an order
    Then the order should be created successfully

  Scenario: Create order for product with highest price
    Given a customer with userId 3 wants to buy product 3
    When the customer creates an order
    Then the order should be created successfully

  Scenario: Attempt to create order for invalid product
    Given a customer with userId 1 wants to buy product 99
    When the customer creates an order
    Then the order creation should fail with error containing "Product not found"
