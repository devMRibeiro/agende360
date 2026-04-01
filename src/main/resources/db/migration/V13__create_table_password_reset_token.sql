CREATE TABLE password_reset_token (
	id UUID PRIMARY KEY,
	user_id UUID NOT NULL,
	token_hash VARCHAR(255) NOT NULL,
	expires_at TIMESTAMP NOT NULL,
	used BOOLEAN DEFAULT FALSE
)