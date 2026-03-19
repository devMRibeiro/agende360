CREATE TABLE users (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    name				VARCHAR(100)    NOT NULL,
    email               VARCHAR(100)    NOT NULL,
    password            VARCHAR(255)    NOT NULL,
    phone               VARCHAR(255)    NOT NULL,
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    role                VARCHAR(50)     NOT NULL,
    company_id          UUID,
    password_chaged_at  TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_company FOREIGN KEY (company_id) REFERENCES company (id)
);

CREATE UNIQUE INDEX idx_users_email ON "users" (email);
CREATE INDEX idx_users_name ON "users" (name);
CREATE INDEX idx_users_phone ON "users" (phone);