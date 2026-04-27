package com.lynx.orderservice.service;

import com.lynx.orderservice.domain.InstrumentType;
import com.lynx.orderservice.domain.Side;
import com.lynx.orderservice.domain.Trade;
import com.lynx.orderservice.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeService Unit Tests")
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private TradeService tradeService;

    private Trade testTrade;
    private UUID tradeId;
    private UUID orderId;
    private UUID platformId;
    private UUID platformUserId;

    @BeforeEach
    void setUp() {
        tradeId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        platformId = UUID.randomUUID();
        platformUserId = UUID.randomUUID();

        testTrade = new Trade(
                tradeId,
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
    }

    @Test
    @DisplayName("Should create a trade successfully")
    void testCreateTrade() {
        when(tradeRepository.save(any(Trade.class))).thenReturn(testTrade);

        Trade result = tradeService.createTrade(testTrade);

        assertThat(result).isNotNull();
        assertThat(result.getTradeId()).isEqualTo(tradeId);
        assertThat(result.getOrderId()).isEqualTo(orderId);
        verify(tradeRepository, times(1)).save(testTrade);
    }

    @Test
    @DisplayName("Should retrieve a trade by ID")
    void testGetTradeById() {
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(testTrade));

        Optional<Trade> result = tradeService.getTradeById(tradeId);

        assertThat(result).isPresent();
        assertThat(result.get().getTradeId()).isEqualTo(tradeId);
        verify(tradeRepository, times(1)).findById(tradeId);
    }

    @Test
    @DisplayName("Should return empty optional when trade not found")
    void testGetTradeByIdNotFound() {
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.empty());

        Optional<Trade> result = tradeService.getTradeById(tradeId);

        assertThat(result).isEmpty();
        verify(tradeRepository, times(1)).findById(tradeId);
    }

    @Test
    @DisplayName("Should retrieve all trades")
    void testGetAllTrades() {
        List<Trade> trades = Arrays.asList(testTrade, testTrade);
        when(tradeRepository.findAll()).thenReturn(trades);

        List<Trade> result = tradeService.getAllTrades();

        assertThat(result).hasSize(2);
        verify(tradeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update a trade")
    void testUpdateTrade() {
        testTrade.setQuantity(new BigDecimal("60"));
        when(tradeRepository.save(any(Trade.class))).thenReturn(testTrade);

        Trade result = tradeService.updateTrade(testTrade);

        assertThat(result.getQuantity()).isEqualTo(new BigDecimal("60"));
        verify(tradeRepository, times(1)).save(testTrade);
    }

    @Test
    @DisplayName("Should delete a trade by ID")
    void testDeleteTrade() {
        tradeService.deleteTrade(tradeId);

        verify(tradeRepository, times(1)).deleteById(tradeId);
    }

    @Test
    @DisplayName("Should find trades by order ID")
    void testGetTradesByOrder() {
        List<Trade> trades = Arrays.asList(testTrade);
        when(tradeRepository.findByOrderId(orderId)).thenReturn(trades);

        List<Trade> result = tradeService.getTradesByOrder(orderId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(orderId);
        verify(tradeRepository, times(1)).findByOrderId(orderId);
    }

    @Test
    @DisplayName("Should find trades by platform")
    void testGetTradesByPlatform() {
        List<Trade> trades = Arrays.asList(testTrade);
        when(tradeRepository.findByPlatformId(platformId)).thenReturn(trades);

        List<Trade> result = tradeService.getTradesByPlatform(platformId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlatformId()).isEqualTo(platformId);
        verify(tradeRepository, times(1)).findByPlatformId(platformId);
    }

    @Test
    @DisplayName("Should find trades by platform and user")
    void testGetTradesByPlatformUser() {
        List<Trade> trades = Arrays.asList(testTrade);
        when(tradeRepository.findByPlatformIdAndPlatformUserId(platformId, platformUserId)).thenReturn(trades);

        List<Trade> result = tradeService.getTradesByPlatformUser(platformId, platformUserId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlatformUserId()).isEqualTo(platformUserId);
        verify(tradeRepository, times(1)).findByPlatformIdAndPlatformUserId(platformId, platformUserId);
    }

    @Test
    @DisplayName("Should find trades by instrument type")
    void testGetTradesByInstrumentType() {
        List<Trade> trades = Arrays.asList(testTrade);
        when(tradeRepository.findByInstrumentType(InstrumentType.STOCK)).thenReturn(trades);

        List<Trade> result = tradeService.getTradesByInstrumentType(InstrumentType.STOCK);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInstrumentType()).isEqualTo(InstrumentType.STOCK);
        verify(tradeRepository, times(1)).findByInstrumentType(InstrumentType.STOCK);
    }

    @Test
    @DisplayName("Should find trades by instrument ID and type (for stocks)")
    void testGetTradesByInstrumentIdAndTypeStock() {
        List<Trade> trades = Arrays.asList(testTrade);
        when(tradeRepository.findByInstrumentIdAndInstrumentType("AAPL", InstrumentType.STOCK)).thenReturn(trades);

        List<Trade> result = tradeService.getTradesByInstrumentIdAndType("AAPL", InstrumentType.STOCK);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInstrumentId()).isEqualTo("AAPL");
        verify(tradeRepository, times(1)).findByInstrumentIdAndInstrumentType("AAPL", InstrumentType.STOCK);
    }

    @Test
    @DisplayName("Should find trades by instrument ID and type (for options)")
    void testGetTradesByInstrumentIdAndTypeOption() {
        Trade optionTrade = new Trade(
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
        List<Trade> trades = Arrays.asList(optionTrade);
        when(tradeRepository.findByInstrumentIdAndInstrumentType("OPT_123456", InstrumentType.OPTION)).thenReturn(trades);

        List<Trade> result = tradeService.getTradesByInstrumentIdAndType("OPT_123456", InstrumentType.OPTION);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInstrumentId()).isEqualTo("OPT_123456");
        assertThat(result.get(0).getInstrumentType()).isEqualTo(InstrumentType.OPTION);
        verify(tradeRepository, times(1)).findByInstrumentIdAndInstrumentType("OPT_123456", InstrumentType.OPTION);
    }

    @Test
    @DisplayName("Should find trades by user and instrument")
    void testGetTradesByUserAndInstrument() {
        List<Trade> trades = Arrays.asList(testTrade);
        when(tradeRepository.findByPlatformUserIdAndInstrumentId(platformUserId, "AAPL")).thenReturn(trades);

        List<Trade> result = tradeService.getTradesByUserAndInstrument(platformUserId, "AAPL");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlatformUserId()).isEqualTo(platformUserId);
        assertThat(result.get(0).getInstrumentId()).isEqualTo("AAPL");
        verify(tradeRepository, times(1)).findByPlatformUserIdAndInstrumentId(platformUserId, "AAPL");
    }

    @Test
    @DisplayName("Should count trades for an order")
    void testCountTradesByOrder() {
        when(tradeRepository.countByOrderId(orderId)).thenReturn(3L);

        long result = tradeService.countTradesByOrder(orderId);

        assertThat(result).isEqualTo(3L);
        verify(tradeRepository, times(1)).countByOrderId(orderId);
    }

    @Test
    @DisplayName("Should return zero when counting trades for order with no executions")
    void testCountTradesByOrderZero() {
        when(tradeRepository.countByOrderId(orderId)).thenReturn(0L);

        long result = tradeService.countTradesByOrder(orderId);

        assertThat(result).isZero();
        verify(tradeRepository, times(1)).countByOrderId(orderId);
    }

    @Test
    @DisplayName("Should indicate order has executions")
    void testHasExecutions() {
        when(tradeRepository.countByOrderId(orderId)).thenReturn(2L);

        boolean result = tradeService.hasExecutions(orderId);

        assertThat(result).isTrue();
        verify(tradeRepository, times(1)).countByOrderId(orderId);
    }

    @Test
    @DisplayName("Should indicate order has no executions")
    void testHasNoExecutions() {
        when(tradeRepository.countByOrderId(orderId)).thenReturn(0L);

        boolean result = tradeService.hasExecutions(orderId);

        assertThat(result).isFalse();
        verify(tradeRepository, times(1)).countByOrderId(orderId);
    }
}

