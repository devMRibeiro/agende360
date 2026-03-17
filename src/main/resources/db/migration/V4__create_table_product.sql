CREATE TABLE product (
    id               UUID             NOT NULL DEFAULT gen_random_uuid(),
    name             VARCHAR(100)     NOT NULL,
    description      TEXT,
    price            NUMERIC(10, 2)   NOT NULL,
    duration_minutes INTEGER          NOT NULL,
    active           BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP        NOT NULL,
    updated_at       TIMESTAMP        NOT NULL,

    CONSTRAINT pk_product PRIMARY KEY (id)
);

CREATE INDEX idx_product_name ON product (name);
