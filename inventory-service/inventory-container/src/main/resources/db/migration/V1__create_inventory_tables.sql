CREATE TABLE IF NOT EXISTS order_inventory (
    product_id BIGINT PRIMARY KEY,
    available_inventory INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS order_inventory_consumption (
    id UUID PRIMARY KEY,
    product_id BIGINT NOT NULL,
    order_id UUID NOT NULL UNIQUE,
    quantity_consumed INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inventory_consumption_order_id ON order_inventory_consumption(order_id);
