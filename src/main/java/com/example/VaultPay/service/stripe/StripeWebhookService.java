package com.example.VaultPay.service.stripe;

import com.example.VaultPay.dao.stripe.DepositRepo;
import com.example.VaultPay.dao.stripe.StripeWebhookEventRepo;
import com.example.VaultPay.model.stripe.Deposit;
import com.example.VaultPay.model.stripe.StripeWebhookEvent;
import com.example.VaultPay.service.EmailService;
import com.example.VaultPay.service.WalletService;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.stripe.model.Event;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StripeWebhookService {

    @Autowired
    private StripeWebhookEventRepo webhookEventRepo;

    @Autowired
    private DepositRepo depositRepo;

    @Autowired
    private WalletService walletService;

    @Autowired
    private EmailService emailService;

    @Transactional
    public void processWebhookEvent(Event event) {

        if (webhookEventRepo.existsByStripeEventId(event.getId())) {
            return; // Already processed
        }

        // Save webhook event
        StripeWebhookEvent webhookEvent = new StripeWebhookEvent();
        webhookEvent.setStripeEventId(event.getId());
        webhookEvent.setEventType(event.getType());
        webhookEvent.setPayload(event.toJson());
        webhookEvent.setProcessed(false);
        webhookEventRepo.save(webhookEvent);


        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "charge.refunded":
                    handleChargeRefunded(event);
                    break;
                default:

                    System.out.println("Unhandled event type: " + event.getType());
            }

            // Mark as processed
            webhookEvent.setProcessed(true);
            webhookEvent.setProcessedAt(LocalDateTime.now());
            webhookEventRepo.save(webhookEvent);

        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            throw e;
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new RuntimeException("Failed to deserialize PaymentIntent"));

        Optional<Deposit> depositOpt = depositRepo.findByStripePaymentIntentId(paymentIntent.getId());

        if (depositOpt.isPresent()) {
            Deposit deposit = depositOpt.get();

            // Update deposit status
            deposit.setStatus("COMPLETED");
            deposit.setCompletedAt(LocalDateTime.now());
            deposit.setStripeChargeId(paymentIntent.getLatestCharge());
            depositRepo.save(deposit);

            // Credit wallet balance
            walletService.creditWalletFromDeposit(deposit.getUser(), deposit.getAmount());

            // Send email notification (async)
            emailService.sendDepositSuccessEmail(deposit);

            System.out.println("Deposit completed: " + deposit.getId() + " for user: " + deposit.getUser().getUsername());
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new RuntimeException("Failed to deserialize PaymentIntent"));

        Optional<Deposit> depositOpt = depositRepo.findByStripePaymentIntentId(paymentIntent.getId());

        if (depositOpt.isPresent()) {
            Deposit deposit = depositOpt.get();

            // Update deposit status
            deposit.setStatus("FAILED");
            deposit.setFailureReason(paymentIntent.getLastPaymentError() != null
                    ? paymentIntent.getLastPaymentError().getMessage()
                    : "Payment failed");
            depositRepo.save(deposit);

            System.out.println("Deposit failed: " + deposit.getId());
        }
    }

    private void handleChargeRefunded(Event event) {
        // Handle refunds if needed
        System.out.println("Charge refunded event received");
    }
}
