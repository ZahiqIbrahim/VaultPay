package com.example.VaultPay.service;

import com.example.VaultPay.dao.WalletRepo;
import com.example.VaultPay.model.Wallet;
import com.example.VaultPay.model.user.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletService {

    @Autowired
    private WalletRepo walletRepo;

    @Transactional
    public Wallet createWallet(User user){
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);

        return walletRepo.save(wallet);
    }

    public Wallet getWallet(User user){
        Wallet wallet = new Wallet();
        wallet = walletRepo.findByUser(user);

        if(wallet == null){
           throw new RuntimeException("Wallet Not Found");
        }
        return wallet;
    }

    public BigDecimal getBalance(User user) {
        Wallet wallet = getWallet(user);

        return wallet.getBalance();
    }
}
