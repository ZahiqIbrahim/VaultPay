package com.example.VaultPay.controller;

import com.example.VaultPay.dao.UserRepo;
import com.example.VaultPay.dao.WalletRepo;
import com.example.VaultPay.dto.SetPinRequest;
import com.example.VaultPay.model.Wallet;
import com.example.VaultPay.model.user.User;
import com.example.VaultPay.service.EmailService;
import com.example.VaultPay.service.OtpService;
import com.example.VaultPay.service.WalletService;
import com.example.VaultPay.service.jwt.JwtService;
import com.example.VaultPay.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private WalletService walletService;

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @GetMapping("/get-wallet")
    public ResponseEntity<?> getWallet(HttpServletRequest request){
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUserName(token);

            User user = userService.findByUsername(username);

            if(user == null){
                return ResponseEntity.status(404).body(Map.of("Error", "User Not Found"));
            }

            return ResponseEntity.ok(Map.of(
                    "Balance", walletService.getBalance(user)
            ));
        }catch (Exception e){
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/set-pin-request")
    public ResponseEntity<?> setPinRequest(HttpServletRequest request){
        try{
            String authHeader = request.getHeader("Authorization");
            if(authHeader == null || !authHeader.startsWith("Bearer ")){
                return ResponseEntity.badRequest().body(Map.of("Error ","Token is required"));
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUserName(token);

            User user = userService.findByUsername(username);

            if(user == null){
                return  ResponseEntity.status(404).body(Map.of("Error", "User not found"));
            }

            otpService.generateAndSendSetPinOtp(user);



        }catch (Exception e){
            return ResponseEntity.status(401).body(Map.of("Error",e.getMessage()));
        }
        return ResponseEntity.ok().body(Map.of("Message","Your Set Pin Otp has been sent on your email"));
    }

    @PostMapping("/set-pin")
    public ResponseEntity<?> setPin(@Valid @RequestBody SetPinRequest request, HttpServletRequest tokenRequest) {
       try{
           String authHeader = tokenRequest.getHeader("Authorization");
           if(authHeader == null || !authHeader.startsWith("Bearer ")){
               return ResponseEntity.badRequest().body(Map.of("Error","Token invalid or expired"));
           }
           String token = authHeader.substring(7);
           String username = jwtService.extractUserName(token);
           User user = userService.findByUsername(username);
           String email = user.getEmail();
           String otp = request.getOtp();
           String pin = request.getPin();
           if(!otpService.verifySetPinOtp(email,otp)){
               return ResponseEntity.badRequest().body(Map.of("Error","Invalid otp"));
           }
           walletService.setWalletPin(user,pin);

       }catch (Exception e){
           return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
       }
       return ResponseEntity.ok().body(Map.of("Message","Your Wallet pin has been set"));
    }
}
