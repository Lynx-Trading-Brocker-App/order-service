package com.lynx.orderservice.repository;

import com.lynx.orderservice.domain.InstrumentType;
import com.lynx.orderservice.domain.Order;
import com.lynx.orderservice.domain.OrderType;
import com.lynx.orderservice.domain.Side;
import com.lynx.orderservice.domain.Status;
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
@DisplayName("OrderRepository Integration Tests")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    private UUID platformId;
    private UUID platformUserId;
    private Order stockOrder;
    private Order optionOrder;

    @BeforeEach
    void setUp() {
        platformId = UUID.randomUUID();
        platformUserId = UUID.randomUUID();

        stockOrder = new Order(
                UUID.randomUUID(),
                platformId,
                platformUserId,
                InstrumentType.STOCK,
                "AAPL",
                OrderType.LIMIT,
                Side.BUY,
                new BigDecimal("100"),
                new BigDecimal("150.00"),
                Status.PENDING,
                BigDecimal.ZERO,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        optionOrder = new Order(
                UUID.randomUUID(),
                platformId,
                platformUserId,
                InstrumentType.OPTION,
                "OPT_123456",
                OrderType.MARKET,
                Side.SELL,
                new BigDecimal("10"),
                null,
                Status.PARTIALLY_FILLED,
                new BigDecimal("10"),
                new BigDecimal("50.00"),
                new BigDecimal("5.00"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }

    @Test
    @DisplayName("Should save and retrieve an order")
    void testSaveAndFindOrder() {
        orderRepository.save(stockOrder);

        Optional<Order> found = orderRepository.findById(stockOrder.getOrderId());

        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo(stockOrder.getOrderId());
        assertThat(found.get().getInstrumentId()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should find orders by platform ID")
    void testFindByPlatformId() {
        orderRepository.save(stockOrder);
        orderRepository.save(optionOrder);

        List<Order> orders = orderRepository.findByPlatformId(platformId);

        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(o -> o.getPlatformId().equals(platformId));
    }

    @Test
    @DisplayName("Should find orders by platform ID and platform user ID")
    void testFindByPlatformIdAndPlatformUserId() {
        orderRepository.save(stockOrder);
        orderRepository.save(optionOrder);

        List<Order> orders = orderRepository.findByPlatformIdAndPlatformUserId(platformId, platformUserId);

        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(o -> o.getPlatformId().equals(platformId) && o.getPlatformUserId().equals(platformUserId));
    }

    @Test
    @DisplayName("Should find orders by platform user ID and status")
    void testFindByPlatformUserIdAndStatus() {
        orderRepository.save(stockOrder);
        orderRepository.save(optionOrder);

        List<Order> pendingOrders = orderRepository.findByPlatformUserIdAndStatus(platformUserId, Status.PENDING);
        List<Order> filledOrders = orderRepository.findByPlatformUserIdAndStatus(platformUserId, Status.PARTIALLY_FILLED);

        assertThat(pendingOrders).hasSize(1);
        assertThat(pendingOrders.getFirst().getStatus()).isEqualTo(Status.PENDING);
        assertThat(filledOrders).hasSize(1);
        assertThat(filledOrders.getFirst().getStatus()).isEqualTo(Status.PARTIALLY_FILLED);
    }

    @Test
    @DisplayName("Should find orders by instrument type")
    void testFindByInstrumentType() {
        orderRepository.save(stockOrder);
        orderRepository.save(optionOrder);

        List<Order> stockOrders = orderRepository.findByInstrumentType(InstrumentType.STOCK);
        List<Order> optionOrders = orderRepository.findByInstrumentType(InstrumentType.OPTION);

        assertThat(stockOrders).hasSize(1);
        assertThat(stockOrders.get(0).getInstrumentType()).isEqualTo(InstrumentType.STOCK);
        assertThat(optionOrders).hasSize(1);
        assertThat(optionOrders.get(0).getInstrumentType()).isEqualTo(InstrumentType.OPTION);
    }

    @Test
    @DisplayName("Should find orders by instrument ID and instrument type for stocks")
    void testFindByInstrumentIdAndInstrumentTypeStock() {
        orderRepository.save(stockOrder);
        orderRepository.save(optionOrder);

        List<Order> aaplOrders = orderRepository.findByInstrumentIdAndInstrumentType("AAPL", InstrumentType.STOCK);

        assertThat(aaplOrders).hasSize(1);
        assertThat(aaplOrders.get(0).getInstrumentId()).isEqualTo("AAPL");
        assertThat(aaplOrders.get(0).getInstrumentType()).isEqualTo(InstrumentType.STOCK);
    }

    @Test
    @DisplayName("Should find orders by instrument ID and instrument type for options")
    void testFindByInstrumentIdAndInstrumentTypeOption() {
        orderRepository.save(stockOrder);
        orderRepository.save(optionOrder);

        List<Order> optOrders = orderRepository.findByInstrumentIdAndInstrumentType("OPT_123456", InstrumentType.OPTION);

        assertThat(optOrders).hasSize(1);
        assertThat(optOrders.get(0).getInstrumentId()).isEqualTo("OPT_123456");
        assertThat(optOrders.get(0).getInstrumentType()).isEqualTo(InstrumentType.OPTION);
    }

    @Test
    @DisplayName("Should find orders by platform ID and status, ordered by creation date")
    void testFindByPlatformIdAndStatus() {
        orderRepository.save(stockOrder);
        orderRepository.save(optionOrder);

        List<Order> filledOrders = orderRepository.findByPlatformIdAndStatus(platformId, Status.PARTIALLY_FILLED);

        assertThat(filledOrders).hasSize(1);
        assertThat(filledOrders.getFirst().getStatus()).isEqualTo(Status.PARTIALLY_FILLED);
    }

    @Test
    @DisplayName("Should find order by order ID and platform ID")
    void testFindByOrderIdAndPlatformId() {
        orderRepository.save(stockOrder);

        Optional<Order> found = orderRepository.findByOrderIdAndPlatformId(stockOrder.getOrderId(), platformId);

        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo(stockOrder.getOrderId());
        assertThat(found.get().getPlatformId()).isEqualTo(platformId);
    }

    @Test
    @DisplayName("Should return empty when order not found by order ID and platform ID")
    void testFindByOrderIdAndPlatformIdNotFound() {
        orderRepository.save(stockOrder);

        UUID differentPlatformId = UUID.randomUUID();
        Optional<Order> found = orderRepository.findByOrderIdAndPlatformId(stockOrder.getOrderId(), differentPlatformId);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should update an order")
    void testUpdateOrder() {
        orderRepository.save(stockOrder);
        stockOrder.setStatus(Status.PARTIALLY_FILLED);
        stockOrder.setFilledQuantity(new BigDecimal("100"));
        orderRepository.save(stockOrder);

        Optional<Order> found = orderRepository.findById(stockOrder.getOrderId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(Status.PARTIALLY_FILLED);
        assertThat(found.get().getFilledQuantity()).isEqualTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("Should delete an order")
    void testDeleteOrder() {
        orderRepository.save(stockOrder);

        orderRepository.deleteById(stockOrder.getOrderId());

        Optional<Order> found = orderRepository.findById(stockOrder.getOrderId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when no orders found for platform")
    void testFindByPlatformIdEmpty() {
        UUID unknownPlatformId = UUID.randomUUID();

        List<Order> orders = orderRepository.findByPlatformId(unknownPlatformId);

        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName("Should find multiple orders for same instrument")
    void testFindMultipleOrdersSameInstrument() {
        Order order1 = new Order(
                UUID.randomUUID(),
                platformId,
                platformUserId,
                InstrumentType.STOCK,
                "AAPL",
                OrderType.LIMIT,
                Side.BUY,
                new BigDecimal("50"),
                new BigDecimal("150.00"),
                Status.PENDING,
                BigDecimal.ZERO,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        Order order2 = new Order(
                UUID.randomUUID(),
                platformId,
                UUID.randomUUID(),
                InstrumentType.STOCK,
                "AAPL",
                OrderType.MARKET,
                Side.SELL,
                new BigDecimal("75"),
                null,
                Status.PARTIALLY_FILLED,
                new BigDecimal("75"),
                new BigDecimal("152.00"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        orderRepository.save(order1);
        orderRepository.save(order2);

        List<Order> aaplOrders = orderRepository.findByInstrumentIdAndInstrumentType("AAPL", InstrumentType.STOCK);

        assertThat(aaplOrders).hasSize(2);
        assertThat(aaplOrders).allMatch(o -> o.getInstrumentId().equals("AAPL"));
    }
}

