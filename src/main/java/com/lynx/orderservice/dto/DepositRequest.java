package com.lynx.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {
    @NotNull(message = "Amount is required")
    @Positive(message = "Deposit amount must be strictly greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;
}
