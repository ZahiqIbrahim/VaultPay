package com.example.VaultPay.service;

import com.example.VaultPay.model.Transaction;
import com.example.VaultPay.model.stripe.Deposit;
import com.example.VaultPay.model.user.User;
import com.example.VaultPay.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private WalletService walletService;

    @Value("${app.email.from}")
    private String adminEmail;

    public void sendOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(adminEmail);
        message.setTo(toEmail);
        message.setSubject("VaultPay - Email Verification");
        message.setText("Your OTP for email verification is: " + otp +
               "\n\nThis OTP will expire in 5 minutes." +
                "\n\nIf you didn't request this, please ignore.");

        mailSender.send(message);

    }

    public void sendResetOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(adminEmail);
        message.setTo(toEmail);
        message.setSubject("VaultPay - Reset Password");
        message.setText("Your OTP for Password Reset is: "+ otp +
                "\n\nThis OTP will expire in 5 minutes." +
                "\n\nIf you didn't request this, please ignore.");
        mailSender.send(message);
    }

    @Async
    public void sendTransferEmail(Transaction transaction, User fromUser, User toUser){

        try {
            BigDecimal amount = transaction.getAmount();
            LocalDateTime completedTime = transaction.getCompletedTime();

            String fromUserName = fromUser.getUsername();
            String toUserName = toUser.getUsername();

            BigDecimal fromUserBalance = walletService.getBalance(fromUser);
            BigDecimal toUserBalance = walletService.getBalance(toUser);

            SimpleMailMessage debitMessage = new SimpleMailMessage();
            debitMessage.setFrom(adminEmail);
            debitMessage.setTo(fromUser.getEmail());
            debitMessage.setSubject("VaultPay - Account Debited");
            debitMessage.setText("Dear " + fromUserName +
                    "\n\nYour Account has been debited by Rs." + amount +
                    " on " + completedTime + " \n\nremaining Account balance is " + fromUserBalance);
            mailSender.send(debitMessage);

            SimpleMailMessage creditMessage = new SimpleMailMessage();
            creditMessage.setFrom(adminEmail);
            creditMessage.setTo(toUser.getEmail());
            creditMessage.setSubject("VaultPay - Account Credited");
            creditMessage.setText("Dear " + toUserName +
                    "\n\nYour Account has been credited by Rs." + amount +
                    " on " + completedTime + " \n\nyour updated Account balance is " + toUserBalance);
            mailSender.send(creditMessage);
        }catch (Exception e){
            throw new RuntimeException("Error Sending the email" + e.getMessage());
        }
    }

    public void sendSetPinOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(adminEmail);
        message.setTo(toEmail);
        message.setSubject("VaultPay - Email Verification");
        message.setText("Your OTP for Setting the pin is: " + otp +
                "\n\nThis OTP will expire in 5 minutes." +
                "\n\nIf you didn't request this, please ignore.");

        mailSender.send(message);

    }

    @Async
    public void sendDepositSuccessEmail(Deposit deposit) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(adminEmail);
            message.setTo(deposit.getUser().getEmail());
            message.setSubject("Deposit Successful - VaultPay");
            message.setText(String.format(
                    "Hello %s,\n\n" +
                            "Your deposit of $%s has been successfully processed!\n\n" +
                            "Transaction ID: %s\n" +
                            "Amount: $%s\n" +
                            "Status: Completed\n\n" +
                            "Your wallet has been credited.\n\n" +
                            "Thank you for using VaultPay!",
                    deposit.getUser().getUsername(),
                    deposit.getAmount(),
                    deposit.getId(),
                    deposit.getAmount()
            ));
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send deposit email: " + e.getMessage());
        }
    }
}
