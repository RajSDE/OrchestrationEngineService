package com.orchestrationengine.repository;

import com.orchestrationengine.model.MessageCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageCodeRepository extends JpaRepository<MessageCode, Integer> {
    Optional<MessageCode> findByServiceCodeAndCode(String serviceCode, String code);
    Optional<MessageCode> findByServiceCodeAndCodeAndLanguage(String serviceCode, String code, String language);
}
