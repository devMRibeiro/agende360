CREATE TABLE appointment (
    id           UUID            NOT NULL DEFAULT gen_random_uuid(),
    company_id   UUID            NOT NULL,
    customer_id  UUID            NOT NULL,
    product_id   UUID            NOT NULL,
    users_id     UUID            NOT NULL,
    start_time   TIMESTAMP       NOT NULL,
    end_time     TIMESTAMP       NOT NULL,
    token        VARCHAR(255),
    status       VARCHAR(50)     NOT NULL,
    created_at   TIMESTAMP       NOT NULL,
    updated_at   TIMESTAMP       NOT NULL,

    CONSTRAINT pk_appointment 			PRIMARY KEY (id),
    CONSTRAINT fk_appointment_company   FOREIGN KEY (company_id)   REFERENCES company (id),
    CONSTRAINT fk_appointment_customer  FOREIGN KEY (customer_id)  REFERENCES customer (id),
    CONSTRAINT fk_appointment_product   FOREIGN KEY (product_id)   REFERENCES product (id),
    CONSTRAINT fk_appointment_users     FOREIGN KEY (users_id)     REFERENCES "users" (id)
);
