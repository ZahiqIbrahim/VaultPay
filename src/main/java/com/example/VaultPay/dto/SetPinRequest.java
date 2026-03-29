package com.example.VaultPay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

public class SetPinRequest {

    @NotBlank(message = "Pin is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "Pin must be 4-6 digits only")
    private String pin;

    @NotBlank(message = "Otp is required")
    @Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits")
    private String otp;
}
