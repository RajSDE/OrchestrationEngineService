package com.orchestrationengine.ums.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Maps to the ` maindb.user_credentials ` table.
 * Stores authentication and credential-specific data.
 */
@Data
@Entity
@Table(name = "user_credentials", schema = "maindb")
public class UserCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key to user_profile table.
     * Establishes ownership of these credentials by a user.
     */
    @Column(name = "user_profile_id", nullable = false, length = 30)
    private String userProfileId;

    /**
     * Unique username for login purposes.
     */
    @Column(name = "username", nullable = false, unique = true, length = 150)
    private String username;

    /**
     * Secure hashed password.
     */
    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    /**
     * When the password was last changed.
     */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    /**
     * Indicates whether MFA (Multi-Factor Auth) is enabled.
     */
    @Column(name = "mfa_enabled")
    private Boolean mfaEnabled = false;

    /**
     * Type of MFA used (e.g., TOTP, SMS, Email).
     */
    @Column(name = "mfa_type", length = 50)
    private String mfaType;

    /**
     * Whether the account is currently locked (e.g., after multiple failed attempts).
     */
    @Column(name = "is_locked")
    private Boolean isLocked = false;

    /**
     * Whether the account is active.
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Timestamp when this record was created.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this record was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Reference to the owning UserProfile entity.
     * Bidirectional mapping if needed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", referencedColumnName = "user_profile_id", insertable = false, updatable = false)
    private UserProfile userProfile;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        passwordChangedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
