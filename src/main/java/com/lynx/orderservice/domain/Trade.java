package com.lynx.orderservice.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the Trade (Execution Record) entity within the application domain.
 * Includes detailed information about a filled execution, such as unique identifiers,
 * {@link InstrumentType}, {@link Side}, execution pricing, and relevant timestamps.
 */
@Entity
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Trade {

    /** The unique identifier for this execution. */
    @Id
    @Column(name = "trade_id", nullable = false, updatable = false)
    private UUID tradeId;

    /** The order this trade belongs to. */
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    /** The broker platform. */
    @Column(name = "platform_id", nullable = false)
    private UUID platformId;

    /** The end user who placed the original order. */
    @Column(name = "platform_user_id", nullable = false)
    private UUID platformUserId;

    /** The type of the instrument (STOCK or OPTION). */
    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false)
    private InstrumentType instrumentType;

    /** The ticker or option_id. */
    @Column(name = "instrument_id", nullable = false)
    private String instrumentId;

    /** The side of the trade (BUY or SELL). */
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private Side side;

    /** The shares or contracts executed in this fill. */
    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    /** The execution price per share or contract. */
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    /** The exchange fee for this execution. */
    @Column(name = "exchange_fee", nullable = false)
    private BigDecimal exchangeFee;

    /** The simulated market timestamp of the execution. */
    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt;
}