drop table password_reset_token;

CREATE TABLE password_reset_token (
	id 					UUID 			NOT NULL DEFAULT gen_random_uuid(),
	user_id 			UUID 			NOT NULL,
	token_hash 			VARCHAR(255)	NOT NULL,
	expires_at 			TIMESTAMP 		NOT NULL,
	used 				BOOLEAN 		DEFAULT FALSE
);