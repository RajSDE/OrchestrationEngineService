package com.orchestrationengine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_request", schema = "maindb")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {

    @Id
    @Column(name = "trace_id", unique = true, nullable = false, length = 100)
    private String traceId;

    @Column(name = "service_code", nullable = false, length = 100)
    private String serviceCode;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime;

    @Column(name = "response_time", nullable = false)
    private LocalDateTime responseTime;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "modified_on")
    private LocalDateTime modifiedOn;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "modified_by", length = 100)
    private String modifiedBy;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdOn = now;
        this.modifiedOn = now;
        if (this.createdBy == null) {
            this.createdBy = "SYSTEM";
        }
        if (this.modifiedBy == null) {
            this.modifiedBy = "SYSTEM";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedOn = LocalDateTime.now();
    }
}
