CREATE TABLE subscription (
    id UUID 					PRIMARY KEY,
    created_at 					TIMESTAMP 			NOT NULL,
    updated_at 					TIMESTAMP 			NOT NULL,
    company_id 					UUID 				NOT NULL UNIQUE,
    plan 						VARCHAR(30) 		NOT NULL,
    status 						VARCHAR(30) 		NOT NULL,
    stripe_customer_id 			VARCHAR(255),
    stripe_subscription_id 		VARCHAR(255),
    current_period_start 		TIMESTAMP,
    current_period_end 			TIMESTAMP,
    cancel_at_period_end 		BOOLEAN 			NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_subscription_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE
);