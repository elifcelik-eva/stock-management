CREATE TABLE IF NOT EXISTS stock_management.product (
                                                        product_id BIGSERIAL PRIMARY KEY,
                                                        product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    product_created_date TIMESTAMP,
    product_updated_date TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
    );