ALTER TABLE product
ADD CONSTRAINT uk_product_name_company UNIQUE (name, company_id);