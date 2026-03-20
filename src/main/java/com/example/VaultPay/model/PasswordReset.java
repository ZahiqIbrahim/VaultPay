package com.example.VaultPay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="password_reset")
@NoArgsConstructor
@AllArgsConstructor
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String otp;
    private LocalDateTime generatedTime;
    private LocalDateTime expiryTime;
    private boolean used = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
