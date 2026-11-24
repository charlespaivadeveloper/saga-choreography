CREATE TABLE IF NOT EXISTS user_balance (
    user_id BIGINT PRIMARY KEY,
    balance DECIMAL(10, 2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS user_transaction (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_id UUID NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_transaction_order_id ON user_transaction(order_id);
CREATE INDEX IF NOT EXISTS idx_user_transaction_user_id ON user_transaction(user_id);
