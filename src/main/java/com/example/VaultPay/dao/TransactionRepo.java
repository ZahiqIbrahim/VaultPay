package com.example.VaultPay.dao;

import com.example.VaultPay.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, Long> {
    Transaction findByFromUserId(Long fromUserId);

    // Get all transactions with user
    @Query("SELECT t FROM Transaction t WHERE t.fromUserId = :userId OR t.toUserId = :userId ORDER BY t.createdTime DESC")
    List<Transaction> findAllByUserId(@Param("userId") Long userId);

    Page<Transaction> findByFromUserIdOrToUserId(
            Long fromUserId,
            Long toUserId,
            Pageable pageable
    );

}
