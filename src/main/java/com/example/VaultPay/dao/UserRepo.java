package com.example.VaultPay.dao;

import com.example.VaultPay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}