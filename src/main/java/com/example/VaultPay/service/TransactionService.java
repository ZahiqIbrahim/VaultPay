package com.example.VaultPay.service;

import com.example.VaultPay.dao.TransactionRepo;
import com.example.VaultPay.dao.WalletRepo;
import com.example.VaultPay.model.Transaction;
import com.example.VaultPay.model.Wallet;
import com.example.VaultPay.model.user.User;
import com.example.VaultPay.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Autowired
    EmailService emailService;

    @Autowired
    WalletRepo walletRepo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Transactional
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
        transaction.setCreatedTime(LocalDateTime.now());
        return transactionRepo.save(transaction);
    }

    @Transactional
    public Transaction transfer(Transaction transaction, String pin){

        try {
            Long fromUserId = transaction.getFromUserId();
            Long toUserId = transaction.getToUserId();

            BigDecimal amount = transaction.getAmount();

            if (fromUserId.equals(toUserId)) {
                throw new RuntimeException(" Cannot transfer to yourself");
            }
            User fromUser = userService.findByUserId(fromUserId);

            Wallet wallet = walletRepo.findByUserWithLock(fromUser);

            if(wallet.getPin() == null){
                throw new RuntimeException(" Pin not set");
            }
            if(!encoder.matches(pin, wallet.getPin())){
                throw new RuntimeException(" Invalid Pin");
            }

            walletService.deductBalance(wallet, amount);

            User toUser = userService.findByUserId(toUserId);
            walletService.incrementBalance(toUser, amount);

            transaction.setStatus(" Completed");
            transaction.setCompletedTime(LocalDateTime.now());
            Transaction savedTransaction = transactionRepo.save(transaction);

            // mail // this email happens asynchronously
            emailService.sendTransferEmail(savedTransaction, fromUser, toUser);

            return savedTransaction;
        } catch (Exception e) {
            transaction.setStatus(" Failed");
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
