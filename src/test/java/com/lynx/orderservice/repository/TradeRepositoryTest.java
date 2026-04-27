package com.lynx.orderservice.repository;

import com.lynx.orderservice.domain.InstrumentType;
import com.lynx.orderservice.domain.Side;
import com.lynx.orderservice.domain.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("TradeRepository Integration Tests")
class TradeRepositoryTest {

    @Autowired
    private TradeRepository tradeRepository;

    private UUID orderId;
    private UUID platformId;
    private UUID platformUserId;
    private Trade stockTrade;
    private Trade optionTrade;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        platformId = UUID.randomUUID();
        platformUserId = UUID.randomUUID();

        stockTrade = new Trade(
                UUID.randomUUID(),
                orderId,
                platformId,
                platformUserId,
                InstrumentType.STOCK,
                "AAPL",
                Side.BUY,
                new BigDecimal("50"),
                new BigDecimal("150.00"),
                new BigDecimal("10.00"),
                LocalDateTime.now()
        );

        optionTrade = new Trade(
                UUID.randomUUID(),
                orderId,
                platformId,
                platformUserId,
                InstrumentType.OPTION,
                "OPT_123456",
                Side.SELL,
                new BigDecimal("10"),
                new BigDecimal("50.00"),
                new BigDecimal("5.00"),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should save and retrieve a trade")
    void testSaveAndFindTrade() {
        tradeRepository.save(stockTrade);

        Optional<Trade> found = tradeRepository.findById(stockTrade.getTradeId());

        assertThat(found).isPresent();
        assertThat(found.get().getTradeId()).isEqualTo(stockTrade.getTradeId());
        assertThat(found.get().getInstrumentId()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should find trades by order ID")
    void testFindByOrderId() {
        tradeRepository.save(stockTrade);
        tradeRepository.save(optionTrade);

        List<Trade> trades = tradeRepository.findByOrderId(orderId);

        assertThat(trades).hasSize(2);
        assertThat(trades).allMatch(t -> t.getOrderId().equals(orderId));
    }

    @Test
    @DisplayName("Should find trades by platform ID")
    void testFindByPlatformId() {
        tradeRepository.save(stockTrade);
        tradeRepository.save(optionTrade);

        List<Trade> trades = tradeRepository.findByPlatformId(platformId);

        assertThat(trades).hasSize(2);
        assertThat(trades).allMatch(t -> t.getPlatformId().equals(platformId));
    }

    @Test
    @DisplayName("Should find trades by platform ID and platform user ID")
    void testFindByPlatformIdAndPlatformUserId() {
        tradeRepository.save(stockTrade);
        tradeRepository.save(optionTrade);

        List<Trade> trades = tradeRepository.findByPlatformIdAndPlatformUserId(platformId, platformUserId);

        assertThat(trades).hasSize(2);
        assertThat(trades).allMatch(t -> t.getPlatformId().equals(platformId) && t.getPlatformUserId().equals(platformUserId));
    }

    @Test
    @DisplayName("Should find trades by instrument type")
    void testFindByInstrumentType() {
        tradeRepository.save(stockTrade);
        tradeRepository.save(optionTrade);

        List<Trade> stockTrades = tradeRepository.findByInstrumentType(InstrumentType.STOCK);
        List<Trade> optionTrades = tradeRepository.findByInstrumentType(InstrumentType.OPTION);

        assertThat(stockTrades).hasSize(1);
        assertThat(stockTrades.get(0).getInstrumentType()).isEqualTo(InstrumentType.STOCK);
        assertThat(optionTrades).hasSize(1);
        assertThat(optionTrades.get(0).getInstrumentType()).isEqualTo(InstrumentType.OPTION);
    }

    @Test
    @DisplayName("Should find trades by instrument ID and instrument type for stocks")
    void testFindByInstrumentIdAndInstrumentTypeStock() {
        tradeRepository.save(stockTrade);
        tradeRepository.save(optionTrade);

        List<Trade> aaplTrades = tradeRepository.findByInstrumentIdAndInstrumentType("AAPL", InstrumentType.STOCK);

        assertThat(aaplTrades).hasSize(1);
        assertThat(aaplTrades.get(0).getInstrumentId()).isEqualTo("AAPL");
        assertThat(aaplTrades.get(0).getInstrumentType()).isEqualTo(InstrumentType.STOCK);
    }

    @Test
    @DisplayName("Should find trades by instrument ID and instrument type for options")
    void testFindByInstrumentIdAndInstrumentTypeOption() {
        tradeRepository.save(stockTrade);
        tradeRepository.save(optionTrade);

        List<Trade> optTrades = tradeRepository.findByInstrumentIdAndInstrumentType("OPT_123456", InstrumentType.OPTION);

        assertThat(optTrades).hasSize(1);
        assertThat(optTrades.get(0).getInstrumentId()).isEqualTo("OPT_123456");
        assertThat(optTrades.get(0).getInstrumentType()).isEqualTo(InstrumentType.OPTION);
    }

    @Test
    @DisplayName("Should find trades by platform user ID and instrument ID, ordered by execution date")
    void testFindByPlatformUserIdAndInstrumentId() {
        tradeRepository.save(stockTrade);

        Trade laterTrade = new Trade(
                UUID.randomUUID(),
                UUID.randomUUID(),
                platformId,
                platformUserId,
                InstrumentType.STOCK,
                "AAPL",
                Side.SELL,
                new BigDecimal("30"),
                new BigDecimal("151.00"),
                new BigDecimal("8.00"),
                LocalDateTime.now().plusHours(1)
        );
        tradeRepository.save(laterTrade);

        List<Trade> aaplTrades = tradeRepository.findByPlatformUserIdAndInstrumentId(platformUserId, "AAPL");

        assertThat(aaplTrades).hasSize(2);
        assertThat(aaplTrades.get(0).getExecutedAt()).isAfterOrEqualTo(aaplTrades.get(1).getExecutedAt());
    }

    @Test
    @DisplayName("Should count trades by order ID")
    void testCountByOrderId() {
        tradeRepository.save(stockTrade);
        tradeRepository.save(optionTrade);

        long count = tradeRepository.countByOrderId(orderId);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should return zero count when no trades for order")
    void testCountByOrderIdZero() {
        UUID unknownOrderId = UUID.randomUUID();

        long count = tradeRepository.countByOrderId(unknownOrderId);

        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Should update a trade")
    void testUpdateTrade() {
        tradeRepository.save(stockTrade);
        stockTrade.setQuantity(new BigDecimal("100"));
        tradeRepository.save(stockTrade);

        Optional<Trade> found = tradeRepository.findById(stockTrade.getTradeId());

        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("Should delete a trade")
    void testDeleteTrade() {
        tradeRepository.save(stockTrade);

        tradeRepository.deleteById(stockTrade.getTradeId());

        Optional<Trade> found = tradeRepository.findById(stockTrade.getTradeId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when no trades found for order")
    void testFindByOrderIdEmpty() {
        UUID unknownOrderId = UUID.randomUUID();

        List<Trade> trades = tradeRepository.findByOrderId(unknownOrderId);

        assertThat(trades).isEmpty();
    }

    @Test
    @DisplayName("Should find multiple trades for same order")
    void testFindMultipleTradesForSameOrder() {
        tradeRepository.save(stockTrade);

        Trade partialFill = new Trade(
                UUID.randomUUID(),
                orderId,
                platformId,
                platformUserId,
                InstrumentType.STOCK,
                "AAPL",
                Side.BUY,
                new BigDecimal("50"),
                new BigDecimal("150.00"),
                new BigDecimal("10.00"),
                LocalDateTime.now().plusMinutes(5)
        );
        tradeRepository.save(partialFill);

        List<Trade> trades = tradeRepository.findByOrderId(orderId);

        assertThat(trades).hasSize(2);
        assertThat(trades).allMatch(t -> t.getOrderId().equals(orderId));
    }

    @Test
    @DisplayName("Should find trades by different platform users")
    void testFindTradesByDifferentUsers() {
        UUID user2Id = UUID.randomUUID();
        Trade user2Trade = new Trade(
                UUID.randomUUID(),
                UUID.randomUUID(),
                platformId,
                user2Id,
                InstrumentType.STOCK,
                "GOOGL",
                Side.BUY,
                new BigDecimal("100"),
                new BigDecimal("2800.00"),
                new BigDecimal("20.00"),
                LocalDateTime.now()
        );

        tradeRepository.save(stockTrade);
        tradeRepository.save(user2Trade);

        List<Trade> user1Trades = tradeRepository.findByPlatformIdAndPlatformUserId(platformId, platformUserId);
        List<Trade> user2Trades = tradeRepository.findByPlatformIdAndPlatformUserId(platformId, user2Id);

        assertThat(user1Trades).hasSize(1);
        assertThat(user2Trades).hasSize(1);
        assertThat(user1Trades.get(0).getPlatformUserId()).isEqualTo(platformUserId);
        assertThat(user2Trades.get(0).getPlatformUserId()).isEqualTo(user2Id);
    }

    @Test
    @DisplayName("Should handle multiple partial fills of same instrument")
    void testMultiplePartialFills() {
        tradeRepository.save(stockTrade);

        Trade fill2 = new Trade(
                UUID.randomUUID(),
                orderId,
                platformId,
                platformUserId,
                InstrumentType.STOCK,
                "AAPL",
                Side.BUY,
                new BigDecimal("30"),
                new BigDecimal("150.50"),
                new BigDecimal("8.00"),
                LocalDateTime.now().plusMinutes(1)
        );

        Trade fill3 = new Trade(
                UUID.randomUUID(),
                orderId,
                platformId,
                platformUserId,
                InstrumentType.STOCK,
                "AAPL",
                Side.BUY,
                new BigDecimal("20"),
                new BigDecimal("150.75"),
                new BigDecimal("5.00"),
                LocalDateTime.now().plusMinutes(2)
        );

        tradeRepository.save(fill2);
        tradeRepository.save(fill3);

        List<Trade> allFills = tradeRepository.findByOrderId(orderId);

        assertThat(allFills).hasSize(3);
        BigDecimal totalQuantity = allFills.stream()
                .map(Trade::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalQuantity).isEqualTo(new BigDecimal("100"));
    }
}

