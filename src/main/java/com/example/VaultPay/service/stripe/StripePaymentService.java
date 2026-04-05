package com.example.VaultPay.service.stripe;

import com.example.VaultPay.dao.stripe.DepositRepo;
import com.example.VaultPay.dto.stripe.DepositRequest;
import com.example.VaultPay.dto.stripe.DepositResponse;
import com.example.VaultPay.model.stripe.Deposit;
import com.example.VaultPay.model.stripe.StripeCustomer;
import com.example.VaultPay.model.user.User;
import com.example.VaultPay.service.WalletService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StripePaymentService {

    @Autowired
    private DepositRepo depositRepo;

    @Autowired
    private StripeCustomerService stripeCustomerService;

    @Autowired
    private WalletService walletService;

    @Value("${app.environment:prod}")
    private String environment;

    @Transactional
    public DepositResponse createDepositIntent(User user, DepositRequest request) throws StripeException {

        System.out.println("Current environment: " + environment);
        
        StripeCustomer stripeCustomer = stripeCustomerService.getOrCreateStripeCustomer(user);

        // Convert amount to Paisa (Stripe uses smallest currency unit)
        long amountInPaisas = request.getAmount().multiply(new BigDecimal("100")).longValue();


        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInPaisas)
                .setCurrency(request.getCurrency())
                .setCustomer(stripeCustomer.getStripeCustomerId())
                .putMetadata("vaultpay_user_id", user.getId().toString())
                .putMetadata("vaultpay_username", user.getUsername())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);


        Deposit deposit = new Deposit();
        deposit.setUser(user);
        deposit.setAmount(request.getAmount());
        deposit.setCurrency(request.getCurrency());
        deposit.setStripePaymentIntentId(paymentIntent.getId());

        // DEV MODE: Skip webhook and credit wallet immediately
        if ("dev".equalsIgnoreCase(environment)) {
            deposit.setStatus("COMPLETED");
            deposit.setCompletedAt(LocalDateTime.now());
            deposit.setStripeChargeId(paymentIntent.getId());
            depositRepo.save(deposit);

            // Credit wallet immediately in dev mode
            walletService.creditWalletFromDeposit(user, request.getAmount());

            System.out.println("DEV MODE: Wallet credited immediately for user: " + user.getUsername());

            return new DepositResponse(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    request.getAmount(),
                    request.getCurrency(),
                    "COMPLETED"
            );
        }

        // PROD MODE: Wait for webhook
        deposit.setStatus("PENDING");
        depositRepo.save(deposit);

        return new DepositResponse(
                paymentIntent.getClientSecret(),
                paymentIntent.getId(),
                request.getAmount(),
                request.getCurrency(),
                "PENDING"
        );
    }

    public Optional<Deposit> findByPaymentIntentId(String paymentIntentId) {
        return depositRepo.findByStripePaymentIntentId(paymentIntentId);
    }

    public Page<Deposit> getDepositHistory(User user, Pageable pageable) {
        return depositRepo.findByUserOrderByCreatedAtDesc(user, pageable);
    }
}
