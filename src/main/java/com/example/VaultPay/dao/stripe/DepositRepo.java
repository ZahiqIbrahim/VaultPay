package com.example.VaultPay.dao.stripe;

import com.example.VaultPay.model.stripe.Deposit;
import com.example.VaultPay.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepositRepo extends JpaRepository<Deposit, Long> {

    Optional<Deposit> findByStripePaymentIntentId(String stripePaymentIntentId);
    Page<Deposit> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
