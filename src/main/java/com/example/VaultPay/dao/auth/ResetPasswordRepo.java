package com.example.VaultPay.dao.auth;

import com.example.VaultPay.model.auth.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ResetPasswordRepo extends JpaRepository<PasswordReset,Long> {

    Optional<PasswordReset> findByEmailAndOtpAndUsedFalse(String email, String otp);
    Optional<PasswordReset> findTopByEmailOrderByGeneratedTimeDesc(String email);

    void deleteByExpiryTimeBefore(LocalDateTime expiryTime);
}
