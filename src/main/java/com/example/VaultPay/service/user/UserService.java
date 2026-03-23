package com.example.VaultPay.service.user;

import com.example.VaultPay.dao.UserRepo;
import com.example.VaultPay.dao.WalletRepo;
import com.example.VaultPay.model.Wallet;
import com.example.VaultPay.service.OtpService;
import com.example.VaultPay.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.VaultPay.model.user.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private OtpService otpService;

    @Autowired
    private WalletService walletService;



    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);


    @Transactional
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

        //generate wallet for the user
        walletService.createWallet(savedUser);

        // Generate and send OTP
        otpService.generateAndSendOtp(savedUser);

        return savedUser;
    }

    @Transactional
    public void changePassword(String email, String password){

        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(encoder.encode(password));
        User savedUser = repo.save(user);
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

        // Generate and send new OTP
        otpService.generateAndSendOtp(user);
    }

    public void resetRequest(String email){
        Optional<User> userOpt = repo.findByEmail(email);
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        otpService.generateAndSendResetOtp(user);
    }

    public boolean verifyResetOtp(String email, String otp){
        return otpService.verifyResetOtp(email, otp);
    }

    public User findByUsername(String username) {
        User user = repo.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

}
