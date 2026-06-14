package com.orchestrationengine.ums.repository;

import com.orchestrationengine.ums.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {
    Optional<UserCredentials> findByUsername(String username);
    Optional<UserCredentials> findByUserProfileId(UUID userProfileId);
    boolean existsByUsername(String username);
    void deleteByUserProfileId(UUID userProfileId);
}
