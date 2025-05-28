package com.ecommerce.service;

import com.ecommerce.controller.OrderController.CreateOrderRequest;
import com.ecommerce.controller.OrderController.OrderSummary;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public interface OrderService {

    // Order creation and management
    Order createOrder(Long userId, CreateOrderRequest request);
    Order updateOrderStatus(Long orderId, OrderStatus status);
    Order cancelOrder(Long orderId, Long userId);

    // Order retrieval
    Optional<Order> getOrderById(Long orderId);
    Optional<Order> getOrderByIdAndUser(Long orderId, Long userId);
    Page<Order> getUserOrders(Long userId, Pageable pageable);
    Page<Order> getOrdersByStatus(Long userId, OrderStatus status, Pageable pageable);
    List<Order> getRecentOrders(Long userId, int limit);

    // Order operations
    Order reorder(Long orderId, Long userId);
    void processOrder(Long orderId);
    void fulfillOrder(Long orderId);
    void shipOrder(Long orderId, String trackingNumber);
    void deliverOrder(Long orderId);

    // Order analytics
    OrderSummary getOrderSummary(Long userId);
    List<Order> getOrdersForAdmin(OrderStatus status, String sortBy, String sortDir);
    long getTotalOrdersCount();
    long getOrdersCountByStatus(OrderStatus status);

    // Order validation
    boolean canCancelOrder(Long orderId, Long userId);
    boolean canReturnOrder(Long orderId, Long userId);
    List<String> validateOrderItems(Long userId);

    // Order notifications
    void sendOrderConfirmation(Long orderId);
    void sendOrderStatusUpdate(Long orderId);
    void sendShippingNotification(Long orderId);
    void sendDeliveryNotification(Long orderId);
}