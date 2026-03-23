package com.example.VaultPay.controller;

import com.example.VaultPay.dao.UserRepo;
import com.example.VaultPay.dao.WalletRepo;
import com.example.VaultPay.model.Wallet;
import com.example.VaultPay.model.user.User;
import com.example.VaultPay.service.WalletService;
import com.example.VaultPay.service.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
public class WalletController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    WalletService walletService;

    @GetMapping("/get-wallet")
    public ResponseEntity<?> getWallet(HttpServletRequest request){
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUserName(token);

            User user = userRepo.findByUsername(username);

            if(user == null){
                return ResponseEntity.status(404).body(Map.of("Error", "User Not Found"));
            }

            return ResponseEntity.ok(Map.of(
                    "Balance", walletService.getBalance(user)
            ));
        }catch (Exception e){
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
    }
}
