package com.lynx.orderservice.service;

import com.lynx.orderservice.domain.InstrumentType;
import com.lynx.orderservice.domain.Order;
import com.lynx.orderservice.domain.Status;
import com.lynx.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for Order entity.
 * Handles business logic, validation, and data retrieval for orders.
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Create and save a new order.
     */
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    /**
     * Retrieve an order by its ID.
     */
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Retrieve all orders.
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Update an existing order.
     */
    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    /**
     * Delete an order by its ID.
     */
    public void deleteOrder(UUID orderId) {
        orderRepository.deleteById(orderId);
    }

    /**
     * Find all orders placed by a specific user on a specific platform.
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByPlatformUser(UUID platformUserId) {
        return orderRepository.findByPlatformUserId(platformUserId);
    }

    /**
     * Find all orders for a specific platform user with a given status.
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserAndStatus(UUID platformUserId, Status status) {
        return orderRepository.findByPlatformUserIdAndStatus(platformUserId, status);
    }

    /**
     * Find all orders of a specific instrument type.
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByInstrumentType(InstrumentType instrumentType) {
        return orderRepository.findByInstrumentType(instrumentType);
    }

    /**
     * Find orders by instrument ID and type.
     * Useful for finding all orders for a specific stock ticker or option ID.
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByInstrumentIdAndType(String instrumentId, InstrumentType instrumentType) {
        return orderRepository.findByInstrumentIdAndInstrumentType(instrumentId, instrumentType);
    }

    /**
     * Find all orders for a specific platform with a given status, ordered by creation date.
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByPlatformAndStatus(UUID platformId, Status status) {
        return orderRepository.findByPlatformIdAndStatus(platformId, status);
    }

    /**
     * Find an order by its ID and platform ID (for validation and security).
     */
    @Transactional(readOnly = true)
    public Optional<Order> getOrderByIdAndPlatform(UUID orderId, UUID platformId) {
        return orderRepository.findByOrderIdAndPlatformId(orderId, platformId);
    }

    /**
     * Check if an order exists for a given platform and user.
     */
    @Transactional(readOnly = true)
    public boolean orderExistsForPlatformUser(UUID orderId, UUID platformId, UUID platformUserId) {
        Optional<Order> order = getOrderByIdAndPlatform(orderId, platformId);
        return order.isPresent() && order.get().getPlatformUserId().equals(platformUserId);
    }
}

