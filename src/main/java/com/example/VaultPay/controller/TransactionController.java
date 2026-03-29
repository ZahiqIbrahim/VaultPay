package com.example.VaultPay.controller;

import com.example.VaultPay.dto.TransferRequest;
import com.example.VaultPay.model.Transaction;
import com.example.VaultPay.model.user.User;
import com.example.VaultPay.service.TransactionService;
import com.example.VaultPay.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @PostMapping("/transfer")
    public ResponseEntity<?> initiate(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody TransferRequest request){
        try{
            String fromUserName = user.getUsername();
            String toUserName = request.getToUserName();
            BigDecimal amount = request.getAmount();
            String remarks = request.getRemarks();
            String pin = request.getPin();
            Transaction transaction = transactionService.createTransaction(fromUserName, toUserName, amount, remarks);
            transactionService.transfer(transaction,pin);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error " + e.getMessage());
        }
        return ResponseEntity.ok("Transaction completed");
    }

    @GetMapping("/transaction-history")
    public ResponseEntity<?> getTransactionHistory(@AuthenticationPrincipal UserDetails user){
        try{
            String username = user.getUsername();
            User currentUser = userService.findByUsername(username);
            List<Map<String, Object>> history = transactionService.getTransactionHistory(currentUser);

            return ResponseEntity.ok(Map.of(
                    "transactions", history,
                    "count", history.size()
            ));

        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error" + e.getMessage());
        }
    }
}
