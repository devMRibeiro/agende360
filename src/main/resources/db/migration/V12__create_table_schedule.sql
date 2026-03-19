CREATE TABLE schedule (
	id 					UUID 			NOT NULL DEFAULT gen_random_uuid(),
	day_of_week			VARCHAR(12) 	NOT NULL,
	start_time  		TIMESTAMP       NOT NULL,
	end_time  			TIMESTAMP       NOT NULL,
	company_id			UUID			NOT NULL,
	created_at  		TIMESTAMP       NOT NULL,
    updated_at  		TIMESTAMP       NOT NULL,

    CONSTRAINT pk_schedule PRIMARY KEY (id),
    CONSTRAINT fk_schedule_company FOREIGN KEY (company_id) REFERENCES company (id)  
);