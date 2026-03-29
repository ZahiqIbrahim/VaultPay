package com.example.VaultPay.model.auth;

import com.example.VaultPay.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "set_pin_otp")
public class SetPin {

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
