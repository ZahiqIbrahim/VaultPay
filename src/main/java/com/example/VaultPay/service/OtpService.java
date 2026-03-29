package com.example.VaultPay.service;

import com.example.VaultPay.dao.SetPinRepo;
import com.example.VaultPay.dao.auth.OtpVerificationRepo;
import com.example.VaultPay.dao.auth.ResetPasswordRepo;
import com.example.VaultPay.model.auth.OtpVerification;
import com.example.VaultPay.model.auth.PasswordReset;
import com.example.VaultPay.model.auth.SetPin;
import com.example.VaultPay.model.user.User;
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
    private ResetPasswordRepo resetRepo;

    @Autowired
    private EmailService emailService;

    @Value("${otp.expiration.minutes:5}")
    private int otpExpirationMinutes;

    @Autowired
    private SetPinRepo setPinRepo;

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

    public void generateAndSendResetOtp(User user){
        String otp = String.format("%06d", new Random().nextInt(999999));

        PasswordReset passwordreset = new PasswordReset();
        passwordreset.setEmail(user.getEmail());
        passwordreset.setOtp(otp);
        passwordreset.setGeneratedTime(LocalDateTime.now());
        passwordreset.setExpiryTime(LocalDateTime.now().plusMinutes(otpExpirationMinutes));
        passwordreset.setUser(user);
        passwordreset.setUsed(false);

        resetRepo.save(passwordreset);
        emailService.sendResetOtpEmail(user.getEmail(),otp);
    }

    public void generateAndSendSetPinOtp(User user){
        String otp = String.format("%06d", new Random().nextInt(999999));

        SetPin setPin = new SetPin();
        setPin.setEmail(user.getEmail());
        setPin.setOtp(otp);
        setPin.setGeneratedTime(LocalDateTime.now());
        setPin.setExpiryTime(LocalDateTime.now().plusMinutes(otpExpirationMinutes));
        setPin.setUser(user);
        setPin.setUsed(false);

        setPinRepo.save(setPin);
        emailService.sendSetPinOtpEmail(user.getEmail(),otp);
    }

    @Transactional
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

    @Transactional
    public boolean verifyResetOtp(String email, String otp){
        var otpRecord = resetRepo.findByEmailAndOtpAndUsedFalse(email, otp);
        if(otpRecord.isEmpty()){
            return false;
        }
        PasswordReset verification = otpRecord.get();
        if(LocalDateTime.now().isAfter(verification.getExpiryTime())){
            return false;
        }

        verification.setUsed(true);
        resetRepo.save(verification);
        return true;
    }

    @Transactional
    public boolean verifySetPinOtp(String email, String otp){
        var otpRecord = setPinRepo.findByEmailAndOtpAndUsedFalse(email,otp);
        if(otpRecord.isEmpty()){
            return false;
        }
        SetPin verification = otpRecord.get();
        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(verification.getExpiryTime())) {
            return false;
        }
        // Mark as verified
        verification.setUsed(true);
        setPinRepo.save(verification);
        return true;
    }

    @Scheduled(cron = "0 0 * * * *") // Runs every hour at minute 0
    @Transactional
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        otpRepo.deleteByExpiryTimeBefore(now);
        resetRepo.deleteByExpiryTimeBefore(now);
        setPinRepo.deleteByExpiryTimeBefore(now);
        System.out.println("Cleaned up expired OTPs at: " + now);
    }
}
