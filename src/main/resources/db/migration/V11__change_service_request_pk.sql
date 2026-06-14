-- V11: Change service_request primary key to trace_id and drop id column

ALTER TABLE maindb.service_request DROP CONSTRAINT IF EXISTS service_request_pkey;
ALTER TABLE maindb.service_request DROP COLUMN IF EXISTS id;
ALTER TABLE maindb.service_request ADD PRIMARY KEY (trace_id);
