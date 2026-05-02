package com.lynx.orderservice.controller;

import com.lynx.orderservice.client.InterServiceClient;
import com.lynx.orderservice.domain.Order;
import com.lynx.orderservice.domain.Side;
import com.lynx.orderservice.domain.Status;
import com.lynx.orderservice.domain.Trade;
import com.lynx.orderservice.dto.*;
import com.lynx.orderservice.service.OrderService;
import com.lynx.orderservice.service.TradeService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
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
    private final RestTemplate restTemplate;

    /**
     * Constructs the OrderController with necessary services and clients.
     *
     * @param orderService       The service for managing Order entities.
     * @param tradeService       The service for managing Trade entities.
     * @param interServiceClient The client for communicating with external services.
     * @param restTemplate       The RestTemplate for HTTP requests.
     */
    public OrderController(OrderService orderService, TradeService tradeService, InterServiceClient interServiceClient, RestTemplate restTemplate) {
        this.orderService = orderService;
        this.tradeService = tradeService;
        this.interServiceClient = interServiceClient;
        this.restTemplate = restTemplate;
    }

    /**
     * Places a new order, saves it locally as PENDING, and forwards it to the Exchange.
     *
     * @param order The order details submitted by the user.
     * @return The created order with its initial status.
     */
    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody Order order) {
        if (order.getSide() == Side.BUY) {
            try {
                ReserveFundsRequest request = new ReserveFundsRequest();
                request.setUserId(order.getPlatformUserId());
                
                // TODO: in the future the price of the stock will be used from the StockExchange for MARKET orders(it's fetched in the api-gateway)
                BigDecimal limitPrice = order.getLimitPrice() != null ? order.getLimitPrice() : BigDecimal.ONE;
                BigDecimal amount = order.getQuantity().multiply(limitPrice);
                request.setAmount(amount);
                request.setCurrency("USD");
                
                restTemplate.postForObject("http://wallet-service:8082/funds/reserve", request, Void.class);
            } catch (Exception e) {
                e.printStackTrace();
                order.setStatus(Status.REJECTED);
                order.setUpdatedAt(LocalDateTime.now());
                Order savedOrder = orderService.createOrder(order);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(savedOrder);
            }
        } else {
            try {
                ReserveQuantityRequest request = new ReserveQuantityRequest();
                request.setUserId(order.getPlatformUserId());
                request.setInstrumentId(order.getInstrumentId());
                request.setQuantity(order.getQuantity());
                
                restTemplate.postForObject("http://portfolio-service:8084/portfolio/reserve", request, Void.class);
            } catch (Exception e) {
                e.printStackTrace();
                order.setStatus(Status.REJECTED);
                order.setUpdatedAt(LocalDateTime.now());
                Order savedOrder = orderService.createOrder(order);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(savedOrder);
            }
        }

        order.setStatus(Status.PENDING);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order savedOrder = orderService.createOrder(order);
        
        // TODO: remove this when stock exchange is done. this is only for testing
        BigDecimal executionPrice = order.getLimitPrice() != null ? order.getLimitPrice() : BigDecimal.ONE;
        OrderStatusUpdateDto mockUpdate = new OrderStatusUpdateDto(
                Status.FILLED, 
                order.getQuantity(), 
                executionPrice, 
                order.getQuantity(), 
                executionPrice, 
                BigDecimal.ZERO
        );
        this.updateOrderStatus(savedOrder.getOrderId(), mockUpdate);

//        interServiceClient.sendOrderToExchange(savedOrder);
        
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
        List<Order> orders = orderService.getOrdersByPlatformUser(userId);
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
        
        //interServiceClient.cancelOrderAtExchange(orderId);
        
        if (order.getSide() == Side.BUY) {
            try {
                ReleaseFundsRequest releaseFundsRequest = new ReleaseFundsRequest();
                releaseFundsRequest.setUserId(order.getPlatformUserId());
                BigDecimal limitPrice = order.getLimitPrice() != null ? order.getLimitPrice() : BigDecimal.ONE;
                BigDecimal amount = order.getQuantity().multiply(limitPrice);
                releaseFundsRequest.setAmount(amount);
                releaseFundsRequest.setCurrency("USD");
                restTemplate.postForObject("http://wallet-service:8082/funds/release", releaseFundsRequest, Void.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                // Assuming release quantity request has similar structure
                ReserveQuantityRequest releaseQuantityRequest = new ReserveQuantityRequest();
                releaseQuantityRequest.setUserId(order.getPlatformUserId());
                releaseQuantityRequest.setInstrumentId(order.getInstrumentId());
                releaseQuantityRequest.setQuantity(order.getQuantity());
                restTemplate.postForObject("http://portfolio-service:8084/portfolio/release", releaseQuantityRequest, Void.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
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
            
            if (order.getSide() == Side.BUY) {
                try {
                    CaptureFundsRequest captureFundsRequest = new CaptureFundsRequest();
                    captureFundsRequest.setUserId(order.getPlatformUserId());
                    BigDecimal limitPrice = order.getLimitPrice() != null ? order.getLimitPrice() : BigDecimal.ONE;
                    BigDecimal reservedAmount = updateDto.executionQuantity().multiply(limitPrice);
                    captureFundsRequest.setReservedAmount(reservedAmount);
                    BigDecimal actualCost = updateDto.executionQuantity().multiply(updateDto.executionPrice());
                    captureFundsRequest.setActualCost(actualCost);
                    captureFundsRequest.setCurrency("USD");
                    restTemplate.postForObject("http://wallet-service:8082/funds/capture", captureFundsRequest, Void.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    AddPositionRequest positionRequest = new AddPositionRequest();
                    positionRequest.setUserId(order.getPlatformUserId());
                    positionRequest.setInstrumentId(order.getInstrumentId());
                    positionRequest.setInstrumentType(order.getInstrumentType().name());
                    positionRequest.setQuantity(updateDto.executionQuantity());
                    positionRequest.setPrice(updateDto.executionPrice());
                    System.out.println("Order-service sent an order with price=" + positionRequest.getPrice());
                    restTemplate.postForObject("http://portfolio-service:8084/portfolio/add", positionRequest, Void.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    CaptureQuantityRequest captureQuantityRequest = new CaptureQuantityRequest();
                    captureQuantityRequest.setUserId(order.getPlatformUserId());
                    captureQuantityRequest.setInstrumentId(order.getInstrumentId());
                    captureQuantityRequest.setQuantity(updateDto.executionQuantity());
                    restTemplate.postForObject("http://portfolio-service:8084/portfolio/capture", captureQuantityRequest, Void.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    DepositRequest depositRequest = new DepositRequest();
                    BigDecimal depositAmount = updateDto.executionQuantity().multiply(updateDto.executionPrice());
                    depositRequest.setAmount(depositAmount);
                    depositRequest.setCurrency("USD");
                    
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("X-User-Id", order.getPlatformUserId().toString());
                    HttpEntity<DepositRequest> entity = new HttpEntity<>(depositRequest, headers);
                    
                    restTemplate.postForObject("http://wallet-service:8082/funds/deposit", entity, Void.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (updateDto.status() == Status.CANCELLED || updateDto.status() == Status.REJECTED || updateDto.status() == Status.EXPIRED) {
            if (order.getSide() == Side.BUY) {
                try {
                    ReleaseFundsRequest releaseFundsRequest = new ReleaseFundsRequest();
                    releaseFundsRequest.setUserId(order.getPlatformUserId());
                    BigDecimal limitPrice = order.getLimitPrice() != null ? order.getLimitPrice() : BigDecimal.ONE;
                    BigDecimal amount = (order.getQuantity().subtract(order.getFilledQuantity())).multiply(limitPrice);
                    releaseFundsRequest.setAmount(amount);
                    releaseFundsRequest.setCurrency("USD");
                    restTemplate.postForObject("http://wallet-service:8082/funds/release", releaseFundsRequest, Void.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    ReserveQuantityRequest releaseQuantityRequest = new ReserveQuantityRequest();
                    releaseQuantityRequest.setUserId(order.getPlatformUserId());
                    releaseQuantityRequest.setInstrumentId(order.getInstrumentId());
                    releaseQuantityRequest.setQuantity(order.getQuantity().subtract(order.getFilledQuantity()));
                    restTemplate.postForObject("http://portfolio-service:8084/portfolio/release", releaseQuantityRequest, Void.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ResponseEntity.ok().build();
    }
}
