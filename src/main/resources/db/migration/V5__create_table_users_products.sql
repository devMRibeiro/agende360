CREATE TABLE users_products (
    users_id     UUID    NOT NULL,
    product_id  UUID    NOT NULL,

    CONSTRAINT pk_users_products 		 PRIMARY KEY (users_id, product_id),
    CONSTRAINT fk_users_products_users    FOREIGN KEY (users_id)    REFERENCES "users" (id),
    CONSTRAINT fk_users_products_product  FOREIGN KEY (product_id) REFERENCES product (id)
);
