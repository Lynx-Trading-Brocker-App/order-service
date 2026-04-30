package com.lynx.orderservice.dto;

import com.lynx.orderservice.domain.Status;

import java.math.BigDecimal;

/**
 * Data Transfer Object for receiving order status updates from the notification service.
 * Carries the new status, cumulative order updates, and specific execution details if a trade occurred.
 *
 * @param status            The updated execution status of the order.
 * @param filledQuantity    The cumulative number of units filled so far.
 * @param averageFillPrice  The updated weighted average price of executed fills.
 * @param executionQuantity The quantity executed in this specific update (used for trade creation).
 * @param executionPrice    The price at which this specific execution occurred (used for trade creation).
 * @param exchangeFee       The fee charged by the exchange for this specific execution.
 */
public record OrderStatusUpdateDto(
        Status status,
        BigDecimal filledQuantity,
        BigDecimal averageFillPrice,
        BigDecimal executionQuantity,
        BigDecimal executionPrice,
        BigDecimal exchangeFee
) {}
