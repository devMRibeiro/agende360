CREATE TABLE customer (
    id          UUID            NOT NULL DEFAULT gen_random_uuid(),
    name 		VARCHAR(100)    NOT NULL,
    phone       VARCHAR(255)    NOT NULL,
    email       VARCHAR(255),
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,

    CONSTRAINT pk_customer PRIMARY KEY (id),
	CONSTRAINT uq_customer_phone UNIQUE (phone)
);

CREATE UNIQUE INDEX idx_customer_phone ON customer (phone);