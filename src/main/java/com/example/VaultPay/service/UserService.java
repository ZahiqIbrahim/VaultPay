package com.example.VaultPay.service;

import com.example.VaultPay.dao.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.VaultPay.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private OtpService otpService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);



    public User registerUser(User user) {
        // Encode password
        user.setPassword(encoder.encode(user.getPassword()));
        user.setEmailVerified(false);

        // Save user
        User savedUser = repo.save(user);

        // Generate and send OTP
        otpService.generateAndSendOtp(savedUser);

        return savedUser;
    }

    public boolean verifyUserEmail(String email, String otp) {
        boolean isValid = otpService.verifyOtp(email, otp);

        if (isValid) {
            User user = repo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setEmailVerified(true);
            repo.save(user);
        }

        return isValid;
    }
}
