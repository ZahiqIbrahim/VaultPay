package com.example.VaultPay.dto.stripe;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Minimum deposit amount is Rs : 1")
    private BigDecimal amount;

    private String currency = "inr";
}
