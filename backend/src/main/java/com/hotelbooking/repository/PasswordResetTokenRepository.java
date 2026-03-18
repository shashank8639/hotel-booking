package com.hotelbooking.repository;

import com.hotelbooking.entity.PasswordResetToken;
import com.hotelbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    void deleteByUser(User user);

    void deleteByUserId(Long userId);
}
