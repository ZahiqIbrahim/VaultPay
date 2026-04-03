package com.example.VaultPay.dao.stripe;

import com.example.VaultPay.model.stripe.StripeWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeWebhookEventRepo extends JpaRepository<StripeWebhookEvent, Long> {
    boolean existsByStripeEventId(String stripeEventId);
}
