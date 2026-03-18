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

        // Check if email already exists
        if (repo.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Check if username already exists
        if (repo.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

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

    public boolean userLogin(String username, String password){
        User user = repo.findByUsername(username);
        if(user == null){
            return false;
        }

        if(!user.getEmailVerified()){
            throw new RuntimeException("Email is not verified, please verify.");
        }

        // Verify password using BCrypt
        return encoder.matches(password, user.getPassword());

    }

    // Resend OTP
    public void resendOtp(String email) {
        // Find the user
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is already verified
        if (user.getEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }

        // Generate and send new OTP
        otpService.generateAndSendOtp(user);
    }
}
