ALTER TABLE product
ADD COLUMN company_id UUID NOT NULL,
ADD CONSTRAINT fk_product_company
FOREIGN KEY (company_id) REFERENCES company(id);