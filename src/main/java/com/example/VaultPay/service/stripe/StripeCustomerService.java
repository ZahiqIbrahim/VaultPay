package com.example.VaultPay.service.stripe;

import com.example.VaultPay.dao.stripe.StripeCustomerRepo;
import com.example.VaultPay.model.stripe.StripeCustomer;
import com.example.VaultPay.model.user.User;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StripeCustomerService {

    @Autowired
    private StripeCustomerRepo stripeCustomerRepo;

    @Transactional
    public StripeCustomer getOrCreateStripeCustomer(User user) throws StripeException {

        Optional<StripeCustomer> existingCustomer = stripeCustomerRepo.findByUser(user);

        if (existingCustomer.isPresent()) {
            return existingCustomer.get();
        }


        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getUsername())
                .setPhone(user.getPhone())
                .putMetadata("vaultpay_user_id", user.getId().toString())
                .build();

        Customer stripeCustomer = Customer.create(params);


        StripeCustomer customer = new StripeCustomer();
        customer.setUser(user);
        customer.setStripeCustomerId(stripeCustomer.getId());

        return stripeCustomerRepo.save(customer);
    }

    public Optional<StripeCustomer> findByUser(User user) {
        return stripeCustomerRepo.findByUser(user);
    }

    public Optional<StripeCustomer> findByStripeCustomerId(String stripeCustomerId) {
        return stripeCustomerRepo.findByStripeCustomerId(stripeCustomerId);
    }

}
