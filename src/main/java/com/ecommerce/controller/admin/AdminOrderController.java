package com.ecommerce.controller.admin;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.enums.OrderStatus;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        List<Order> orders = orderService.getOrdersForAdmin(status, sortBy, sortDir);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody @Valid UpdateOrderStatusRequest request) {
        Order order = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/process")
    public ResponseEntity<Void> processOrder(@PathVariable Long orderId) {
        orderService.processOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/fulfill")
    public ResponseEntity<Void> fulfillOrder(@PathVariable Long orderId) {
        orderService.fulfillOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<Void> shipOrder(
            @PathVariable Long orderId,
            @RequestBody @Valid ShipOrderRequest request) {
        orderService.shipOrder(orderId, request.getTrackingNumber());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<Void> deliverOrder(@PathVariable Long orderId) {
        orderService.deliverOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Order>> getPendingOrders() {
        List<Order> orders = orderService.getOrdersForAdmin(OrderStatus.PENDING, "orderDate", "asc");
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/processing")
    public ResponseEntity<List<Order>> getProcessingOrders() {
        List<Order> orders = orderService.getOrdersForAdmin(OrderStatus.PROCESSING, "orderDate", "asc");
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getOrderAnalytics() {
        Map<String, Object> analytics = Map.of(
                "totalOrders", orderService.getTotalOrdersCount(),
                "pendingOrders", orderService.getOrdersCountByStatus(OrderStatus.PENDING),
                "processingOrders", orderService.getOrdersCountByStatus(OrderStatus.PROCESSING),
                "shippedOrders", orderService.getOrdersCountByStatus(OrderStatus.SHIPPED),
                "deliveredOrders", orderService.getOrdersCountByStatus(OrderStatus.DELIVERED),
                "cancelledOrders", orderService.getOrdersCountByStatus(OrderStatus.CANCELLED)
        );
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Order>> searchOrders(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Implementation needed for order search
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/bulk-update")
    public ResponseEntity<String> bulkUpdateOrders(@RequestBody BulkUpdateOrdersRequest request) {
        // Implementation needed for bulk order updates
        return ResponseEntity.ok("Bulk update completed");
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "csv") String format) {
        // Implementation needed for order export
        return ResponseEntity.ok("Export completed");
    }

    @PostMapping("/{orderId}/send-notification")
    public ResponseEntity<Void> sendOrderNotification(@PathVariable Long orderId) {
        orderService.sendOrderStatusUpdate(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/timeline")
    public ResponseEntity<List<OrderTimelineEvent>> getOrderTimeline(@PathVariable Long orderId) {
        // Implementation needed for order timeline
        return ResponseEntity.ok(List.of());
    }

    // DTOs for admin order operations
    public static class UpdateOrderStatusRequest {
        private OrderStatus status;
        private String notes;

        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class ShipOrderRequest {
        private String trackingNumber;
        private String carrier;
        private String notes;

        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
        public String getCarrier() { return carrier; }
        public void setCarrier(String carrier) { this.carrier = carrier; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class BulkUpdateOrdersRequest {
        private List<Long> orderIds;
        private OrderStatus status;
        private String action;

        public List<Long> getOrderIds() { return orderIds; }
        public void setOrderIds(List<Long> orderIds) { this.orderIds = orderIds; }
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }

    public static class OrderTimelineEvent {
        private String event;
        private String timestamp;
        private String description;
        private String performedBy;

        public OrderTimelineEvent(String event, String timestamp, String description, String performedBy) {
            this.event = event;
            this.timestamp = timestamp;
            this.description = description;
            this.performedBy = performedBy;
        }

        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    }
}