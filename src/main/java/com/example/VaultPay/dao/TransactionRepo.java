package com.example.VaultPay.dao;

import com.example.VaultPay.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, Long> {

}
