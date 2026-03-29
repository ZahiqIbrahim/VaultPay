package com.example.VaultPay.service;

import com.example.VaultPay.dao.TransactionRepo;
import com.example.VaultPay.dao.WalletRepo;
import com.example.VaultPay.model.Wallet;
import com.example.VaultPay.model.user.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.beans.Encoder;
import java.math.BigDecimal;

@Service
public class WalletService {

    @Autowired
    private WalletRepo walletRepo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

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
           throw new RuntimeException(" Wallet Not Found");
        }
        return wallet;
    }

    public BigDecimal getBalance(User user) {
        Wallet wallet = getWallet(user);
        return wallet.getBalance();
    }
    public boolean checkBalance(User user, BigDecimal amount){
        //no locking
        Wallet wallet = getWallet(user);
        if(wallet.getBalance()==null){
            throw new RuntimeException(" Balance is Null");
        }
        if(wallet.getBalance().compareTo(amount) >= 0){
            return true;
        }
        return false;
    }


    @Transactional
    public BigDecimal deductBalance(Wallet wallet, BigDecimal amount){

        BigDecimal balance = wallet.getBalance();
            if(wallet.getBalance().compareTo(amount) >= 0){
                wallet.setBalance(balance.subtract(amount));
            }else{
                throw new RuntimeException(" Insufficient balance");
            }
            walletRepo.save(wallet);

        return wallet.getBalance();
    }

    @Transactional
    public BigDecimal incrementBalance(User user, BigDecimal amount){
        //locked
        Wallet wallet = walletRepo.findByUserWithLock(user);

        if(wallet == null){
            throw new RuntimeException(" Wallet Not Found");
        }

        BigDecimal balance = wallet.getBalance();
        wallet.setBalance(balance.add(amount));

        walletRepo.save(wallet);

        return wallet.getBalance();
    }

    @Transactional
    public void setWalletPin(User user, String pin){
        Wallet wallet = getWallet(user);
        wallet.setPin(encoder.encode(pin));
        walletRepo.save(wallet);
    }
}
