package com.example.VaultPay.service;

import com.example.VaultPay.dao.TransactionRepo;
import com.example.VaultPay.model.Transaction;
import com.example.VaultPay.model.Wallet;
import com.example.VaultPay.model.user.User;
import com.example.VaultPay.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService {

    @Autowired
    WalletService walletService;

    @Autowired
    UserService userService;

    @Autowired
    TransactionRepo transactionRepo;

    public Transaction createTransaction(String fromUsername, String toUsername, BigDecimal amount, String remarks){
        Transaction transaction = new Transaction();
        User fromUser = userService.findByUsername(fromUsername);
        User toUser = userService.findByUsername(toUsername);
        transaction.setFromUserId(fromUser.getId());
        transaction.setToUserId(toUser.getId());
        transaction.setAmount(amount);
        if(remarks != null){
            transaction.setRemarks(remarks);
        }
        transaction.setStatus("Processing");

        return transactionRepo.save(transaction);
    }

    @Transactional
    public Transaction transfer(Transaction transaction){

        try {Long fromUserId = transaction.getFromUserId();
            Long toUserId = transaction.getToUserId();
            BigDecimal amount = transaction.getAmount();
            if (fromUserId.equals(toUserId)) {
                throw new RuntimeException("Cannot transfer to yourself");
            }
            User fromUser = userService.findByUserId(fromUserId);
            walletService.deductBalance(fromUser, amount);

            User toUser = userService.findByUserId(toUserId);
            walletService.incrementBalance(toUser, amount);
            transaction.setStatus("Completed");
            transactionRepo.save(transaction);
            // mail
            return transaction;

        } catch (Exception e) {
            transaction.setStatus("Failed");
            transactionRepo.save(transaction);
            throw e;
        }

    }
}
