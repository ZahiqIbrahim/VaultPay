package com.example.VaultPay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("codeorg51@gmail.com");
        message.setTo(toEmail);
        message.setSubject("ValutPay - Email Verification");
        message.setText("Your OTP for email verification is: " + otp +
               "\n\nThis OTP will expire in 5 minutes." +
                "\n\nIf you didn't request this, please ignore.");

        mailSender.send(message);

    }
}
