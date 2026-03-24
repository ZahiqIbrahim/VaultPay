package com.example.VaultPay.service;

import com.example.VaultPay.dao.TransactionRepo;
import com.example.VaultPay.model.Transaction;
import com.example.VaultPay.model.user.User;
import com.example.VaultPay.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


        if(!walletService.checkBalance(fromUser,amount)){
            transaction.setStatus("Failed");
            transactionRepo.save(transaction);
            throw new RuntimeException(" Insufficient balance");
        }
        transaction.setStatus("Processing");
        transaction.setCreatedTime(LocalDateTime.now());
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
            transaction.setCompletedTime(LocalDateTime.now());
            transactionRepo.save(transaction);
            // mail
            return transaction;

        } catch (Exception e) {
            transaction.setStatus("Failed");
            transactionRepo.save(transaction);
            throw e;
        }

    }

    public  List<Map<String, Object>> getTransactionHistory(User user) {
        Long userId = user.getId();
        List<Transaction> transactions = transactionRepo.findAllByUserId(userId);

        List<Map<String, Object>> history = new ArrayList<>();

        for (Transaction t : transactions) {
            Map<String, Object> txn = new HashMap<>();
            txn.put("transactionId", t.getId());
            txn.put("amount", t.getAmount());
            txn.put("status", t.getStatus());
            txn.put("remarks", t.getRemarks());
            txn.put("createdTime", t.getCreatedTime());
            txn.put("completedTime", t.getCompletedTime());

            // Add direction and other party
            if (t.getFromUserId().equals(userId)) {
                txn.put("type", "SENT");
                User toUser = userService.findByUserId(t.getToUserId());
                txn.put("otherParty", toUser.getUsername());
            } else {
                txn.put("type", "RECEIVED");
                User fromUser = userService.findByUserId(t.getFromUserId());
                txn.put("otherParty", fromUser.getUsername());
            }

            history.add(txn);
        }

        return history;
    }
}
