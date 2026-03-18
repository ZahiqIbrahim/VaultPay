package com.example.VaultPay.dao;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email;
    private String otp;
}
