CREATE TABLE company_settings (
	id 					UUID 			NOT NULL 	DEFAULT gen_random_uuid(),
	company_id 			UUID 			NOT NULL,
	created_at  		TIMESTAMP       NOT NULL,
    updated_at  		TIMESTAMP       NOT NULL,
    scheduling_horizon  SMALLINT 		NOT NULL	DEFAULT 0,
    
    CONSTRAINT pk_company_settings PRIMARY KEY (id),
    CONSTRAINT fk_company_settings_company FOREIGN KEY (company_id) REFERENCES company (id)
);