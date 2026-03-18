package com.example.VaultPay.dao;

import com.example.VaultPay.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepo extends JpaRepository<OtpVerification,Long> {
    Optional<OtpVerification> findByEmailAndOtpAndVerifiedFalse(String email, String otp);
    Optional<OtpVerification> findTopByEmailOrderByGeneratedTimeDesc(String email);

    void deleteByExpiryTimeBefore(LocalDateTime expiryTime);
}
