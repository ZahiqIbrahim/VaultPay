package com.example.VaultPay.dao.stripe;

import com.example.VaultPay.model.stripe.StripeCustomer;
import com.example.VaultPay.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StripeCustomerRepo extends JpaRepository<StripeCustomer,Long> {

    Optional<StripeCustomer> findByUser(User user);
    Optional<StripeCustomer> findByStripeCustomerId(String StripeCustomerId);
}
