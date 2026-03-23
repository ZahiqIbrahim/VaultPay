package com.example.VaultPay.controller;


import com.example.VaultPay.dto.LoginRequest;
import com.example.VaultPay.dto.ResetRequest;
import com.example.VaultPay.dto.ResetRequestEmail;
import com.example.VaultPay.model.auth.RefreshToken;
import com.example.VaultPay.model.user.User;
import com.example.VaultPay.service.jwt.JwtService;
import com.example.VaultPay.service.jwt.RefreshTokenService;
import com.example.VaultPay.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Autowired
    private RefreshTokenService refreshTokenService;

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
            String accessToken = jwtService.generateAccessToken(request.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());

            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken.getToken(),
                    "tokenType", "Bearer"
            ));
        }else{
            return ResponseEntity.badRequest().body(Map.of("error", "Login Failed"));
        }

    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token is required"));
        }

        String refreshToken = authHeader.substring(7);

        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateAccessToken(user.getUsername());
                    return ResponseEntity.ok(Map.of(
                            "accessToken", accessToken,
                            "refreshToken", refreshToken,
                            "tokenType", "Bearer"
                    ));
                })
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
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
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUserName(token);

        User user = service.findByUsername(username);
        refreshTokenService.deleteByUser(user);

        return ResponseEntity.ok(Map.of("message", "Logout successful!"));
    }

}
