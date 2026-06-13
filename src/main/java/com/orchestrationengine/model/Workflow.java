package com.orchestrationengine.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity mapping to workflows metadata table.
 * Controls service code definitions and their runtime toggles.
 */
@Entity
@Table(name = "workflows", schema = "maindb")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workflow {

    @Id
    @Column(name = "service_code", length = 100)
    private String serviceCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "enabled", length = 1)
    private String enabled;

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
        if (this.enabled == null) {
            this.enabled = "Y";
        }
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
