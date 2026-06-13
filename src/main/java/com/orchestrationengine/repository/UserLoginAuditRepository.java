package com.orchestrationengine.repository;

import com.orchestrationengine.ums.entity.UserLoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserLoginAuditRepository extends JpaRepository<UserLoginAudit, Long> {
    void deleteByUserProfileId(UUID userProfileId);
}
