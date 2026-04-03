package com.example.VaultPay.service.stripe;

import com.example.VaultPay.dao.stripe.DepositRepo;
import com.example.VaultPay.dto.stripe.DepositRequest;
import com.example.VaultPay.dto.stripe.DepositResponse;
import com.example.VaultPay.model.stripe.Deposit;
import com.example.VaultPay.model.stripe.StripeCustomer;
import com.example.VaultPay.model.user.User;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class StripePaymentService {

    @Autowired
    private DepositRepo depositRepo;

    @Autowired
    private StripeCustomerService stripeCustomerService;

    @Transactional
    public DepositResponse createDepositIntent(User user, DepositRequest request) throws StripeException {

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
        deposit.setStatus("PENDING");
        deposit.setStripePaymentIntentId(paymentIntent.getId());
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
