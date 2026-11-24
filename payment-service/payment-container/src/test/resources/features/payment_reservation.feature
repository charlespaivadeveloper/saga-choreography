Feature: Payment Reservation
  As the payment service
  I want to process payment reservation events from the SAGA
  So that I can reserve or reject payment for an order

  Scenario: Successfully process payment for a user with sufficient balance
    Given a user with userId 1 wants to pay for product 1
    When the payment is processed
    Then the payment should be processed without errors

  Scenario: Successfully process payment for a different user
    Given a user with userId 2 wants to pay for product 2
    When the payment is processed
    Then the payment should be processed without errors

  Scenario: Fail to process payment for a non-existent user
    Given a user with userId 999 wants to pay for product 1
    When the payment is processed
    Then the payment processing should fail with error containing "User not found"

  Scenario: Verify user balance exists for seeded user
    Given a user with userId 1 wants to pay for product 1
    Then the user balance for userId 1 should exist
