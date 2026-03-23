ALTER TABLE customer
DROP CONSTRAINT fk_customer_company,
DROP COLUMN company_id;

ALTER TABLE customer
ADD CONSTRAINT uq_customer_phone UNIQUE (phone);

CREATE UNIQUE INDEX idx_customer_phone ON customer (phone);