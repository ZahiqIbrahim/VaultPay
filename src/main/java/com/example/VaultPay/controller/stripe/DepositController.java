package com.example.VaultPay.controller.stripe;

import com.example.VaultPay.dto.stripe.DepositRequest;
import com.example.VaultPay.dto.stripe.DepositResponse;
import com.example.VaultPay.model.stripe.Deposit;
import com.example.VaultPay.model.user.User;
import com.example.VaultPay.service.stripe.StripePaymentService;
import com.example.VaultPay.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class DepositController {

    @Autowired
    private StripePaymentService stripePaymentService;

    @Autowired
    private UserService userService;

    @PostMapping("/create-intent")
    public ResponseEntity<?> createDepositIntent(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody DepositRequest request) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            DepositResponse response = stripePaymentService.createDepositIntent(user, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to create deposit: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getDepositHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            Pageable pageable = PageRequest.of(page, size);
            Page<Deposit> deposits = stripePaymentService.getDepositHistory(user, pageable);

            return ResponseEntity.ok(Map.of(
                    "deposits", deposits.getContent(),
                    "currentPage", deposits.getNumber(),
                    "totalPages", deposits.getTotalPages(),
                    "totalItems", deposits.getTotalElements()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
