package com.lynx.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AddPositionRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Instrument ID is required")
    private String instrumentId;

    @NotBlank(message = "Instrument type is required")
    private String instrumentType;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be strictly greater than 0")
    private BigDecimal price;
}
