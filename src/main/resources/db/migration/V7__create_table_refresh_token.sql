CREATE TABLE refresh_token (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    token        VARCHAR(255) NOT NULL,
    users_id     UUID         NOT NULL,
    expires_at   TIMESTAMP    NOT NULL,
    revoked      BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_users FOREIGN KEY (users_id) REFERENCES "users" (id)
);
