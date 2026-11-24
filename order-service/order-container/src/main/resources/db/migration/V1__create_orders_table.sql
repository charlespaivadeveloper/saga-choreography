CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    order_status VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50),
    inventory_status VARCHAR(50),
    failure_messages TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_order_status ON orders(order_status);
