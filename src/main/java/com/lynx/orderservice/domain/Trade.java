package com.lynx.orderservice.domain;

import jakarta.persistence.*;
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
public class Trade {

    @Id
    @Column(name = "trade_id", nullable = false, updatable = false)
    private UUID trade_id;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID order_id;

    @Column(name = "platform_id", nullable = false)
    private UUID platform_id;

    @Column(name = "platform_user_id", nullable = false)
    private UUID platform_user_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false)
    private InstrumentType instrument_type;

    @Column(name = "instrument_id", nullable = false)
    private String instrument_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private Side side;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "exchange_fee", nullable = false)
    private BigDecimal exchange_fee;

    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executed_at;

    /**
     * Constructor without arguments required by Hibernate.
     */
    protected Trade() {}

    /**
     * Constructs a fully initialized Trade object.
     * <p>
     * This constructor is typically used when the trade execution record is retrieved from
     * the database or when manually instantiating a complete record after a market fill.
     * </p>
     *
     * @param trade_id the unique identifier for this execution
     * @param order_id the order this trade belongs to
     * @param platform_id the broker platform
     * @param platform_user_id the end user who placed the original order
     * @param instrument_type the type of the instrument (STOCK or OPTION)
     * @param instrument_id the ticker or option_id
     * @param side the side of the trade (BUY or SELL)
     * @param quantity the shares or contracts executed in this fill
     * @param price the execution price per share or contract
     * @param exchange_fee the exchange fee for this execution
     * @param executed_at the simulated market timestamp of the execution
     */
    public Trade(UUID trade_id, UUID order_id, UUID platform_id, UUID platform_user_id,
                 InstrumentType instrument_type, String instrument_id, Side side,
                 BigDecimal quantity, BigDecimal price, BigDecimal exchange_fee,
                 LocalDateTime executed_at) {
        this.trade_id = trade_id;
        this.order_id = order_id;
        this.platform_id = platform_id;
        this.platform_user_id = platform_user_id;
        this.instrument_type = instrument_type;
        this.instrument_id = instrument_id;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.exchange_fee = exchange_fee;
        this.executed_at = executed_at;
    }

    /**
     * Returns the unique identifier for this execution.
     * @return the trade's ID
     */
    public UUID getTrade_id() {
        return trade_id;
    }

    /**
     * Sets or updates the unique identifier for this execution.
     * @param trade_id the new ID of the trade
     */
    public void setTrade_id(UUID trade_id) {
        this.trade_id = trade_id;
    }

    /**
     * Returns the identifier of the order this trade belongs to.
     * @return the order's ID
     */
    public UUID getOrder_id() {
        return order_id;
    }

    /**
     * Sets or updates the identifier of the order this trade belongs to.
     * @param order_id the new order ID
     */
    public void setOrder_id(UUID order_id) {
        this.order_id = order_id;
    }

    /**
     * Returns the platform ID that submitted the original order.
     * @return the platform's ID
     */
    public UUID getPlatform_id() {
        return platform_id;
    }

    /**
     * Sets or updates the platform ID that submitted the original order.
     * @param platform_id the new platform ID
     */
    public void setPlatform_id(UUID platform_id) {
        this.platform_id = platform_id;
    }

    /**
     * Returns the user ID on the platform who placed the order.
     * @return the user's ID
     */
    public UUID getPlatform_user_id() {
        return platform_user_id;
    }

    /**
     * Sets or updates the user ID on the platform who placed the order.
     * @param platform_user_id the new user ID
     */
    public void setPlatform_user_id(UUID platform_user_id) {
        this.platform_user_id = platform_user_id;
    }

    /**
     * Returns the {@link InstrumentType} of the trade.
     * @return the instrument type (e.g., STOCK, OPTION)
     */
    public InstrumentType getInstrument_type() {
        return instrument_type;
    }

    /**
     * Sets or updates the {@link InstrumentType} of the trade.
     * @param instrument_type the new instrument type
     */
    public void setInstrument_type(InstrumentType instrument_type) {
        this.instrument_type = instrument_type;
    }

    /**
     * Returns the identifier of the specific instrument (e.g., ticker or option_id).
     * @return the instrument's ID
     */
    public String getInstrument_id() {
        return instrument_id;
    }

    /**
     * Sets or updates the identifier of the specific instrument.
     * @param instrument_id the new instrument ID
     */
    public void setInstrument_id(String instrument_id) {
        this.instrument_id = instrument_id;
    }

    /**
     * Returns the {@link Side} of the trade (BUY or SELL).
     * @return the trade's side
     */
    public Side getSide() {
        return side;
    }

    /**
     * Sets or updates the {@link Side} of the trade.
     * @param side the new side
     */
    public void setSide(Side side) {
        this.side = side;
    }

    /**
     * Returns the quantity of shares or contracts executed in this fill.
     * @return the executed quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Sets or updates the quantity of shares or contracts executed in this fill.
     * @param quantity the new executed quantity
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    /**
     * Returns the execution price per share or contract.
     * @return the execution price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets or updates the execution price per share or contract.
     * @param price the new execution price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Returns the exchange fee charged for this specific execution.
     * @return the exchange fee
     */
    public BigDecimal getExchange_fee() {
        return exchange_fee;
    }

    /**
     * Sets or updates the exchange fee charged for this specific execution.
     * @param exchange_fee the new exchange fee
     */
    public void setExchange_fee(BigDecimal exchange_fee) {
        this.exchange_fee = exchange_fee;
    }

    /**
     * Returns the simulated market timestamp when the execution occurred.
     * @return the execution timestamp
     */
    public LocalDateTime getExecuted_at() {
        return executed_at;
    }

    /**
     * Sets or updates the simulated market timestamp of the execution.
     * @param executed_at the new execution timestamp
     */
    public void setExecuted_at(LocalDateTime executed_at) {
        this.executed_at = executed_at;
    }
}