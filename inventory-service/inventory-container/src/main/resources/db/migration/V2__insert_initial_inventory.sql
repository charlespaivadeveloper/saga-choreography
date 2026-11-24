INSERT INTO order_inventory (product_id, available_inventory) VALUES
    (1, 5),
    (2, 5),
    (3, 5)
ON CONFLICT (product_id) DO NOTHING;
