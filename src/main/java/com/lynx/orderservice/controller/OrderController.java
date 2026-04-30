package com.lynx.orderservice.controller;

import com.lynx.orderservice.client.InterServiceClient;
import com.lynx.orderservice.domain.Order;
import com.lynx.orderservice.domain.Status;
import com.lynx.orderservice.domain.Trade;
import com.lynx.orderservice.dto.OrderStatusUpdateDto;
import com.lynx.orderservice.service.OrderService;
import com.lynx.orderservice.service.TradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing orders and processing status updates.
 * Exposes endpoints for user-facing order operations and system-facing execution updates.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final TradeService tradeService;
    private final InterServiceClient interServiceClient;

    /**
     * Constructs the OrderController with necessary services and clients.
     *
     * @param orderService       The service for managing Order entities.
     * @param tradeService       The service for managing Trade entities.
     * @param interServiceClient The client for communicating with external services.
     */
    public OrderController(OrderService orderService, TradeService tradeService, InterServiceClient interServiceClient) {
        this.orderService = orderService;
        this.tradeService = tradeService;
        this.interServiceClient = interServiceClient;
    }

    /**
     * Places a new order, saves it locally as PENDING, and forwards it to the Exchange.
     *
     * @param order The order details submitted by the user.
     * @return The created order with its initial status.
     */
    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody Order order) {
        order.setStatus(Status.PENDING);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order savedOrder = orderService.createOrder(order);
        interServiceClient.sendOrderToExchange(savedOrder);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    /**
     * Retrieves an order by its unique identifier.
     *
     * @param orderId The unique identifier of the order.
     * @return The requested order.
     * @throws ResponseStatusException if the order is not found.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
                
        return ResponseEntity.ok(order);
    }

    /**
     * Retrieves all orders associated with a specific user.
     *
     * @param userId     The unique identifier of the platform user.
     * @return A list of orders belonging to the user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable UUID userId) {
        UUID platformId = UUID.fromString("PLATFORM_ID"); // replace this. idk if this should be done automatically. I hope not
        List<Order> orders = orderService.getOrdersByPlatformUser(platformId, userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Cancels an existing order, updates its local status, and notifies the Exchange.
     *
     * @param orderId The unique identifier of the order to be cancelled.
     * @return A response indicating successful cancellation.
     * @throws ResponseStatusException if the order is not found.
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
                
        order.setStatus(Status.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderService.updateOrder(order);
        
        interServiceClient.cancelOrderAtExchange(orderId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Processes an incoming status update for an order from the Notification Service.
     * If the update indicates a fill, generates a Trade record and notifies related services.
     *
     * @param orderId   The unique identifier of the order being updated.
     * @param updateDto The data transfer object containing the new status and execution details.
     * @return A response confirming the update was processed.
     * @throws ResponseStatusException if the order is not found.
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody OrderStatusUpdateDto updateDto) {
            
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        order.setStatus(updateDto.status());
        order.setFilledQuantity(updateDto.filledQuantity());
        order.setAverageFillPrice(updateDto.averageFillPrice());
        order.setUpdatedAt(LocalDateTime.now());
        
        orderService.updateOrder(order);

        if (updateDto.status() == Status.FILLED || updateDto.status() == Status.PARTIALLY_FILLED) {
            Trade trade = new Trade(
                    UUID.randomUUID(),
                    order.getOrderId(),
                    order.getPlatformId(),
                    order.getPlatformUserId(),
                    order.getInstrumentType(),
                    order.getInstrumentId(),
                    order.getSide(),
                    updateDto.executionQuantity(),
                    updateDto.executionPrice(),
                    updateDto.exchangeFee(),
                    LocalDateTime.now()
            );
            
            Trade savedTrade = tradeService.createTrade(trade);
            
            interServiceClient.notifyFeeService(savedTrade);
            interServiceClient.notifyPortfolioService(savedTrade);
            interServiceClient.notifyWalletService(savedTrade);
        }

        return ResponseEntity.ok().build();
    }
}
