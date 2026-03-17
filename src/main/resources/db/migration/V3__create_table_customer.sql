CREATE TABLE customer (
    id          UUID            NOT NULL DEFAULT gen_random_uuid(),
    phone       VARCHAR(255)    NOT NULL,
    email       VARCHAR(255)    NOT NULL,
    company_id  UUID,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,

    CONSTRAINT pk_customer PRIMARY KEY (id),
    CONSTRAINT uq_customer_email UNIQUE (email),
    CONSTRAINT fk_customer_company FOREIGN KEY (company_id) REFERENCES company (id)
);

CREATE INDEX idx_customer_phone ON customer (phone);
CREATE UNIQUE INDEX idx_customer_email ON customer (email);
