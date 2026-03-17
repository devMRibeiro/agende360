CREATE TABLE company (
    id          UUID            NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(255)    NOT NULL,
    slug        VARCHAR(255)    NOT NULL,
    document    VARCHAR(20)     NOT NULL,
    active      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,

    CONSTRAINT pk_company PRIMARY KEY (id),
    CONSTRAINT uq_company_slug UNIQUE (slug),
    CONSTRAINT uq_company_document UNIQUE (document)
);

CREATE INDEX idx_company_name ON company (name);
CREATE UNIQUE INDEX idx_company_slug ON company (slug);
