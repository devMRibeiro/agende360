ALTER TABLE schedule
ALTER COLUMN start_time TYPE TIME USING start_time::TIME,
ALTER COLUMN end_time TYPE TIME USING end_time::TIME;