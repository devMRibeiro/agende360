CREATE TABLE outbox_event (
    id 				UUID 		PRIMARY KEY,
    aggregate_id 	UUID 						NOT NULL,
    aggregate_type 	VARCHAR(50) 				NOT NULL,
    event_type 		VARCHAR(100) 				NOT NULL,
    payload 		JSONB 						NOT NULL,
    event_status 	VARCHAR(30) 				NOT NULL,
    created_at 		TIMESTAMPTZ 				NOT NULL,
    sent_at 		TIMESTAMPTZ,
    retry_count 	INTEGER 					NOT NULL 	DEFAULT 0,
    next_attempt_at TIMESTAMPTZ,
    last_error 		TEXT
);

CREATE INDEX idx_outbox_status_next_attempt ON outbox_event (event_status, next_attempt_at);
CREATE INDEX idx_outbox_status_created      ON outbox_event (event_status, created_at);