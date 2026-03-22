package com.example.VaultPay.controller;


import com.example.VaultPay.dto.LoginRequest;
import com.example.VaultPay.dto.ResetRequest;
import com.example.VaultPay.dto.ResetRequestEmail;
import com.example.VaultPay.model.User;
import com.example.VaultPay.service.JwtService;
import com.example.VaultPay.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    AuthenticationManager authenticationmanager;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            User registeredUser = service.registerUser(user);
            return ResponseEntity.ok(Map.of(
                    "message", "Registration successful! Please check your email for OTP.",
                    "email", registeredUser.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        boolean isVerified = service.verifyUserEmail(email, otp);

        if (isVerified) {
            return ResponseEntity.ok(Map.of("message", "Email verified successfully!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired OTP"));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        Authentication authentication = authenticationmanager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        if(authentication.isAuthenticated()){
            return ResponseEntity.ok(Map.of("JWT", jwtService.generateToken(request.getUsername())));
        }else{
            return ResponseEntity.badRequest().body(Map.of("error", "Login Failed"));
        }

    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            service.resendOtp(email);

            return ResponseEntity.ok(Map.of("message", "OTP resent successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/resetPassword-Request")
    public ResponseEntity<?> resetPasswordRequest(@Valid @RequestBody ResetRequestEmail request){
        try {
            String email = request.getEmail();
            service.resetRequest(email);
            return ResponseEntity.ok("Reset OTP sent successfully!");
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body("Error " + e.getMessage());
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetRequest request){
        try {
            String email = request.getEmail();
            String otp = request.getOtp();
            boolean isVerified = service.verifyResetOtp(email, otp);
            if (!isVerified) {
                return ResponseEntity.badRequest().body("Error ");
            }
            String password = request.getNewPassword();
            service.changePassword(email,password);
            return ResponseEntity.ok("Password changed successfully!");

        }catch(RuntimeException e) {
            return ResponseEntity.badRequest().body("Error " + e.getMessage());
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logout successful!"));
    }

}
