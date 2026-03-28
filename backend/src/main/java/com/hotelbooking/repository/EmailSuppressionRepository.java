package com.hotelbooking.repository;

import com.hotelbooking.entity.EmailSuppression;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailSuppressionRepository extends JpaRepository<EmailSuppression, Long> {

    Optional<EmailSuppression> findByEmailIgnoreCaseAndActiveTrue(String email);

    boolean existsByEmailIgnoreCaseAndActiveTrue(String email);
}
