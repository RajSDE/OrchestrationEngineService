package com.orchestrationengine.ums.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_login_audit", schema = "maindb")
public class UserLoginAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_profile_id", nullable = false, length = 30)
    private String userProfileId;

    @Column(name = "login_attempt_time")
    private LocalDateTime loginAttemptTime;

    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "failure_reason")
    private String failureReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", referencedColumnName = "user_profile_id", insertable = false, updatable = false)
    private UserProfile userProfile;

    @PrePersist
    protected void onCreate() {
        loginAttemptTime = LocalDateTime.now();
    }
}
