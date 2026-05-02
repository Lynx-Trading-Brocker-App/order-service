package com.lynx.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CaptureQuantityRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Instrument ID is required")
    private String instrumentId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be strictly greater than 0")
    private BigDecimal quantity;
}
