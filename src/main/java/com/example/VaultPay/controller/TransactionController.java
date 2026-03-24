package com.example.VaultPay.controller;

import com.example.VaultPay.dto.TransferRequest;
import com.example.VaultPay.model.Transaction;
import com.example.VaultPay.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<?> initiate(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody TransferRequest request){
        try{
            String fromUserName = user.getUsername();
            String toUserName = request.getToUserName();
            BigDecimal amount = request.getAmount();
            String remarks = request.getRemarks();

            Transaction transaction = transactionService.createTransaction(fromUserName, toUserName, amount, remarks);
            transactionService.transfer(transaction);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error" + e.getMessage());
        }
        return ResponseEntity.ok("Transaction completed");
    }
}
