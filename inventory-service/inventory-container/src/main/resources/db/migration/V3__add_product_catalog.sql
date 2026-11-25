-- Product catalog table: enriches the existing product_id key with full catalog data.
-- order_inventory already holds stock counts (product_id BIGINT PRIMARY KEY).
-- After seeding the 3 products that match V2 entries, we enforce the FK.

CREATE TABLE product (
    id              BIGINT          PRIMARY KEY,
    sku             VARCHAR(100)    NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    category        VARCHAR(100)    NOT NULL,
    price           NUMERIC(10, 2)  NOT NULL,
    weight_grams    INT,
    attributes      JSONB           NOT NULL DEFAULT '{}',
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_product_sku     UNIQUE (sku),
    CONSTRAINT chk_price_positive CHECK  (price > 0)
);

-- Seed data aligned with V2 order_inventory entries (ids 1, 2, 3)
INSERT INTO product (id, sku, name, description, category, price, weight_grams, attributes) VALUES
(1,
 'PROD-SMARTPHONE-001',
 'Smartphone Galaxy X',
 'Smartphone premium com tela AMOLED 6.5", câmera 108 MP, bateria 5000 mAh e 128 GB de armazenamento.',
 'ELECTRONICS',
 2999.99,
 185,
 '{"color": "Black", "storage": "128GB", "battery": "5000mAh", "display": "6.5\" AMOLED", "camera_mp": 108}'
),
(2,
 'PROD-HEADPHONE-001',
 'Fone Bluetooth Pro',
 'Fone de ouvido sem fio com cancelamento ativo de ruído (ANC) e autonomia de 30 horas.',
 'ELECTRONICS',
 299.99,
 250,
 '{"color": "White", "connectivity": "Bluetooth 5.0", "battery_hours": 30, "noise_cancelling": true}'
),
(3,
 'PROD-BACKPACK-001',
 'Mochila Profissional 30 L',
 'Mochila resistente em Nylon 600D com compartimento acolchoado para notebooks de até 17".',
 'ACCESSORIES',
 189.99,
 820,
 '{"material": "Nylon 600D", "capacity_liters": 30, "color": "Black", "max_laptop_inches": 17}'
),
(4,
 'PROD-KEYBOARD-001',
 'Teclado Mecânico RGB',
 'Teclado mecânico compacto TKL com switches Blue, iluminação RGB e cabo removível.',
 'ELECTRONICS',
 349.99,
 650,
 '{"switch_type": "Blue", "layout": "TKL", "rgb": true, "cable": "detachable", "color": "Space Gray"}'
),
(5,
 'PROD-MOUSEPAD-001',
 'Mousepad XL Gaming',
 'Mousepad extra large 90 × 40 cm com base antiderrapante e bordas costuradas.',
 'ACCESSORIES',
 89.99,
 410,
 '{"dimensions_cm": "90x40", "thickness_mm": 3, "color": "Black/Red", "stitched_edges": true}'
);

-- FK: every existing order_inventory row must reference a valid product
ALTER TABLE order_inventory
    ADD CONSTRAINT fk_order_inventory_product
    FOREIGN KEY (product_id) REFERENCES product(id);

-- Query indexes
CREATE INDEX idx_product_category_active ON product(category) WHERE active = TRUE;
CREATE INDEX idx_product_active          ON product(active);

-- Full-text search on name + description (Portuguese dictionary)
CREATE INDEX idx_product_fts ON product
    USING gin(to_tsvector('portuguese', name || ' ' || COALESCE(description, '')));
