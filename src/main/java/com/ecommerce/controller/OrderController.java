package com.ecommerce.controller;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.OrderStatus;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreateOrderRequest request) {
        Order order = orderService.createOrder(user.getId(), request);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<Page<Order>> getUserOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderService.getUserOrders(user.getId(), pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId) {
        Optional<Order> order = orderService.getOrderByIdAndUser(orderId, user.getId());
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId) {
        Order order = orderService.cancelOrder(orderId, user.getId());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Order>> getOrdersByStatus(
            @AuthenticationPrincipal User user,
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderService.getOrdersByStatus(user.getId(), status, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Order>> getRecentOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "5") int limit) {
        List<Order> orders = orderService.getRecentOrders(user.getId(), limit);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/reorder")
    public ResponseEntity<Order> reorder(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId) {
        Order newOrder = orderService.reorder(orderId, user.getId());
        return ResponseEntity.ok(newOrder);
    }

    @GetMapping("/summary")
    public ResponseEntity<OrderSummary> getOrderSummary(@AuthenticationPrincipal User user) {
        OrderSummary summary = orderService.getOrderSummary(user.getId());
        return ResponseEntity.ok(summary);
    }

    // DTOs for order operations
    public static class CreateOrderRequest {
        private Long shippingAddressId;
        private String paymentMethod;
        private String couponCode;
        private String notes;

        public Long getShippingAddressId() { return shippingAddressId; }
        public void setShippingAddressId(Long shippingAddressId) { this.shippingAddressId = shippingAddressId; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getCouponCode() { return couponCode; }
        public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class OrderSummary {
        private long totalOrders;
        private long pendingOrders;
        private long completedOrders;
        private long cancelledOrders;
        private BigDecimal totalSpent;
        private BigDecimal averageOrderValue;

        public OrderSummary(long totalOrders, long pendingOrders, long completedOrders,
                            long cancelledOrders, BigDecimal totalSpent, BigDecimal averageOrderValue) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.completedOrders = completedOrders;
            this.cancelledOrders = cancelledOrders;
            this.totalSpent = totalSpent;
            this.averageOrderValue = averageOrderValue;
        }

        // Getters and setters
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
        public long getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
        public long getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }
        public long getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }
        public BigDecimal getTotalSpent() { return totalSpent; }
        public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
        public BigDecimal getAverageOrderValue() { return averageOrderValue; }
        public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    }
}