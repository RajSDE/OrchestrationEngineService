-- V10: Create service_request table for API auditing

CREATE TABLE maindb.service_request
(
    id               UUID PRIMARY KEY,
    trace_id         VARCHAR(100) NOT NULL,
    service_code     VARCHAR(100) NOT NULL,
    request_payload  TEXT,
    response_payload TEXT,
    status           VARCHAR(20)  NOT NULL,
    request_time     TIMESTAMP    NOT NULL,
    response_time    TIMESTAMP    NOT NULL,
    duration_ms      BIGINT       NOT NULL,
    created_on       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_on      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(100) DEFAULT 'SYSTEM',
    modified_by      VARCHAR(100) DEFAULT 'SYSTEM'
);

CREATE INDEX idx_service_request_trace ON maindb.service_request (trace_id);
