INSERT INTO user_balance (user_id, balance) VALUES
    (1, 1000.00),
    (2, 1000.00),
    (3, 1000.00)
ON CONFLICT (user_id) DO NOTHING;
