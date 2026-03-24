package com.example.VaultPay.dao;

import com.example.VaultPay.model.Wallet;
import com.example.VaultPay.model.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepo extends JpaRepository< Wallet , Long> {

    // For READ operations (no lock)
    Wallet findByUser(User user);


    // For WRITE operations (with lock)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user = :user")
    Wallet findByUserWithLock(@Param("user") User user);

}
