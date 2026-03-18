package com.example.VaultPay.service;

import com.example.VaultPay.dao.OtpVerificationRepo;
import com.example.VaultPay.model.OtpVerification;
import com.example.VaultPay.model.User;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpVerificationRepo otpRepo;

    @Autowired
    private EmailService emailService;

    @Value("${otp.expiration.minutes:5}")
    private int otpExpirationMinutes;

    public void generateAndSendOtp(User user){
        // Generate 6 digit otp
        String otp = String.format("%06d", new Random().nextInt(999999));

        //save otp to database
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(user.getEmail());
        otpVerification.setOtp(otp);
        otpVerification.setGeneratedTime(LocalDateTime.now());
        otpVerification.setExpiryTime(LocalDateTime.now().plusMinutes(otpExpirationMinutes));
        otpVerification.setUser(user);
        otpVerification.setVerified(false);

        otpRepo.save(otpVerification);

        //Send email

        emailService.sendOtpEmail(user.getEmail(),otp);


    }

    public boolean verifyOtp(String email, String otp){
        var otpRecord = otpRepo.findByEmailAndOtpAndVerifiedFalse(email,otp);

        if(otpRecord.isEmpty()){
            return false;
        }

        OtpVerification verification = otpRecord.get();

        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(verification.getExpiryTime())) {
            return false;
        }

        // Mark as verified
        verification.setVerified(true);
        otpRepo.save(verification);

        return true;

    }

    @Scheduled(cron = "0 0 * * * *") // Runs every hour at minute 0
    @Transactional
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        otpRepo.deleteByExpiryTimeBefore(now);
        System.out.println("Cleaned up expired OTPs at: " + now);
    }
}
