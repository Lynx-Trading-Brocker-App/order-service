package com.lynx.orderservice.service;

import com.lynx.orderservice.domain.InstrumentType;
import com.lynx.orderservice.domain.Order;
import com.lynx.orderservice.domain.OrderType;
import com.lynx.orderservice.domain.Side;
import com.lynx.orderservice.domain.Status;
import com.lynx.orderservice.repository.OrderRepository;
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
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private UUID orderId;
    private UUID platformId;
    private UUID platformUserId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        platformId = UUID.randomUUID();
        platformUserId = UUID.randomUUID();

        testOrder = new Order(
                orderId,
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
    }

    @Test
    @DisplayName("Should create an order successfully")
    void testCreateOrder() {
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.createOrder(testOrder);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getInstrumentId()).isEqualTo("AAPL");
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Should retrieve an order by ID")
    void testGetOrderById() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        Optional<Order> result = orderService.getOrderById(orderId);

        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(orderId);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Should return empty optional when order not found")
    void testGetOrderByIdNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getOrderById(orderId);

        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Should retrieve all orders")
    void testGetAllOrders() {
        List<Order> orders = Arrays.asList(testOrder, testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.getAllOrders();

        assertThat(result).hasSize(2);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update an order")
    void testUpdateOrder() {
        testOrder.setStatus(Status.PARTIALLY_FILLED);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrder(testOrder);

        assertThat(result.getStatus()).isEqualTo(Status.PARTIALLY_FILLED);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Should delete an order by ID")
    void testDeleteOrder() {
        orderService.deleteOrder(orderId);

        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    @DisplayName("Should find orders by platform")
    void testGetOrdersByPlatform() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByPlatformId(platformId)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByPlatformUser(platformId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlatformId()).isEqualTo(platformId);
        verify(orderRepository, times(1)).findByPlatformId(platformId);
    }

    @Test
    @DisplayName("Should find orders by platform and user")
    void testGetOrdersByPlatformUser() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByPlatformUserId(platformUserId)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByPlatformUser(platformUserId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlatformUserId()).isEqualTo(platformUserId);
        verify(orderRepository, times(1)).findByPlatformUserId(platformUserId);
    }

    @Test
    @DisplayName("Should find orders by user and status")
    void testGetOrdersByUserAndStatus() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByPlatformUserIdAndStatus(platformUserId, Status.PENDING)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByUserAndStatus(platformUserId, Status.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Status.PENDING);
        verify(orderRepository, times(1)).findByPlatformUserIdAndStatus(platformUserId, Status.PENDING);
    }

    @Test
    @DisplayName("Should find orders by instrument type")
    void testGetOrdersByInstrumentType() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByInstrumentType(InstrumentType.STOCK)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByInstrumentType(InstrumentType.STOCK);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInstrumentType()).isEqualTo(InstrumentType.STOCK);
        verify(orderRepository, times(1)).findByInstrumentType(InstrumentType.STOCK);
    }

    @Test
    @DisplayName("Should find orders by instrument ID and type (for stocks)")
    void testGetOrdersByInstrumentIdAndTypeStock() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByInstrumentIdAndInstrumentType("AAPL", InstrumentType.STOCK)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByInstrumentIdAndType("AAPL", InstrumentType.STOCK);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInstrumentId()).isEqualTo("AAPL");
        verify(orderRepository, times(1)).findByInstrumentIdAndInstrumentType("AAPL", InstrumentType.STOCK);
    }

    @Test
    @DisplayName("Should find orders by instrument ID and type (for options)")
    void testGetOrdersByInstrumentIdAndTypeOption() {
        Order optionOrder = new Order(
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
        List<Order> orders = Arrays.asList(optionOrder);
        when(orderRepository.findByInstrumentIdAndInstrumentType("OPT_123456", InstrumentType.OPTION)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByInstrumentIdAndType("OPT_123456", InstrumentType.OPTION);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInstrumentId()).isEqualTo("OPT_123456");
        assertThat(result.get(0).getInstrumentType()).isEqualTo(InstrumentType.OPTION);
        verify(orderRepository, times(1)).findByInstrumentIdAndInstrumentType("OPT_123456", InstrumentType.OPTION);
    }

    @Test
    @DisplayName("Should find orders by platform and status")
    void testGetOrdersByPlatformAndStatus() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByPlatformIdAndStatus(platformId, Status.PENDING)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByPlatformAndStatus(platformId, Status.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Status.PENDING);
        verify(orderRepository, times(1)).findByPlatformIdAndStatus(platformId, Status.PENDING);
    }

    @Test
    @DisplayName("Should find order by ID and platform")
    void testGetOrderByIdAndPlatform() {
        when(orderRepository.findByOrderIdAndPlatformId(orderId, platformId)).thenReturn(Optional.of(testOrder));

        Optional<Order> result = orderService.getOrderByIdAndPlatform(orderId, platformId);

        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(orderId);
        assertThat(result.get().getPlatformId()).isEqualTo(platformId);
        verify(orderRepository, times(1)).findByOrderIdAndPlatformId(orderId, platformId);
    }

    @Test
    @DisplayName("Should verify order exists for platform user")
    void testOrderExistsForPlatformUser() {
        when(orderRepository.findByOrderIdAndPlatformId(orderId, platformId)).thenReturn(Optional.of(testOrder));

        boolean result = orderService.orderExistsForPlatformUser(orderId, platformId, platformUserId);

        assertThat(result).isTrue();
        verify(orderRepository, times(1)).findByOrderIdAndPlatformId(orderId, platformId);
    }

    @Test
    @DisplayName("Should return false when order does not exist for platform user")
    void testOrderDoesNotExistForPlatformUser() {
        when(orderRepository.findByOrderIdAndPlatformId(orderId, platformId)).thenReturn(Optional.empty());

        boolean result = orderService.orderExistsForPlatformUser(orderId, platformId, platformUserId);

        assertThat(result).isFalse();
        verify(orderRepository, times(1)).findByOrderIdAndPlatformId(orderId, platformId);
    }

    @Test
    @DisplayName("Should return false when order belongs to different user")
    void testOrderExistsForDifferentUser() {
        when(orderRepository.findByOrderIdAndPlatformId(orderId, platformId)).thenReturn(Optional.of(testOrder));

        UUID differentUserId = UUID.randomUUID();
        boolean result = orderService.orderExistsForPlatformUser(orderId, platformId, differentUserId);

        assertThat(result).isFalse();
        verify(orderRepository, times(1)).findByOrderIdAndPlatformId(orderId, platformId);
    }
}

