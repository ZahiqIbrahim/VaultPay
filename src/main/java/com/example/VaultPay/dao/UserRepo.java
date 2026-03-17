package com.example.VaultPay.dao;

import com.example.VaultPay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface UserRepo extends JpaRepository<User, Integer> {
    User findByUsername(String username);
}