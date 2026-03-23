package com.example.VaultPay.dao;

import com.example.VaultPay.model.Wallet;
import com.example.VaultPay.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepo extends JpaRepository< Wallet , Long> {

    Wallet findByUser(User user);

}
