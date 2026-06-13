package com.orchestrationengine.repository;

import com.orchestrationengine.ums.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    Optional<UserAuth> findByRefreshToken(String refreshToken);
    void deleteByUserProfileId(UUID userProfileId);
}
