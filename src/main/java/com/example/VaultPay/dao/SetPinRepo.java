package com.example.VaultPay.dao;

import com.example.VaultPay.model.auth.PasswordReset;
import com.example.VaultPay.model.auth.SetPin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.script.ScriptTemplateConfig;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SetPinRepo extends JpaRepository<SetPin,Long> {

    Optional<SetPin> findByEmailAndOtpAndUsedFalse(String email, String otp);

    void deleteByExpiryTimeBefore(LocalDateTime expiryTime);
}
