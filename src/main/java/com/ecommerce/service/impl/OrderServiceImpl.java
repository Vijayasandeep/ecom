package com.ecommerce.service.impl;

import com.ecommerce.controller.OrderController.CreateOrderRequest;
import com.ecommerce.controller.OrderController.OrderSummary;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.OrderStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public Order createOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get cart items
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        // Calculate total and create order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());

            order.getOrderItems().add(orderItem);

            BigDecimal itemTotal = cartItem.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteByUserId(userId);

        return savedOrder;
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel shipped or delivered order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderByIdAndUser(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getUserOrders(Long userId, Pageable pageable) {
        return (Page<Order>) orderRepository.findByUserIdOrderByOrderDateDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStatus(Long userId, OrderStatus status, Pageable pageable) {
        return orderRepository.findByUserIdAndStatusOrderByOrderDateDesc(userId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getRecentOrders(Long userId, int limit) {
        return orderRepository.findRecentOrdersByUser(userId, limit);
    }

    @Override
    public Order reorder(Long orderId, Long userId) {
        Order originalOrder = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Clear current cart
        cartItemRepository.deleteByUserId(userId);

        // Add order items back to cart
        for (OrderItem orderItem : originalOrder.getOrderItems()) {
            CartItem cartItem = new CartItem();
            cartItem.setUser(originalOrder.getUser());
            cartItem.setProduct(orderItem.getProduct());
            cartItem.setQuantity(orderItem.getQuantity());
            cartItemRepository.save(cartItem);
        }

        // Create new order request
        CreateOrderRequest request = new CreateOrderRequest();
        return createOrder(userId, request);
    }

    @Override
    public void processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in pending status");
        }

        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
    }

    @Override
    public void fulfillOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new BadRequestException("Order is not in processing status");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Override
    public void shipOrder(Long orderId, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(OrderStatus.SHIPPED);
        // Would store tracking number in additional field
        orderRepository.save(order);
    }

    @Override
    public void deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new BadRequestException("Order is not in shipped status");
        }

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderSummary getOrderSummary(Long userId) {
        long totalOrders = orderRepository.countByUserId(userId);
        long pendingOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.PENDING);
        long completedOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.CANCELLED);

        BigDecimal totalSpent = orderRepository.getTotalSpentByUser(userId);
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;

        BigDecimal averageOrderValue = orderRepository.getAverageOrderValueByUser(userId);
        if (averageOrderValue == null) averageOrderValue = BigDecimal.ZERO;

        return new OrderSummary(totalOrders, pendingOrders, completedOrders,
                cancelledOrders, totalSpent, averageOrderValue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersForAdmin(OrderStatus status, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        if (status != null) {
            return orderRepository.findByStatus(status);
        } else {
            return orderRepository.findAll(sort);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getOrdersCountByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCancelOrder(Long orderId, Long userId) {
        Optional<Order> orderOpt = orderRepository.findByIdAndUserId(orderId, userId);
        if (orderOpt.isEmpty()) {
            return false;
        }

        Order order = orderOpt.get();
        return order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReturnOrder(Long orderId, Long userId) {
        Optional<Order> orderOpt = orderRepository.findByIdAndUserId(orderId, userId);
        if (orderOpt.isEmpty()) {
            return false;
        }

        Order order = orderOpt.get();
        return order.getStatus() == OrderStatus.DELIVERED;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> validateOrderItems(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        List<String> errors = new java.util.ArrayList<>();

        for (CartItem item : cartItems) {
            if (!item.getProduct().getActive()) {
                errors.add("Product " + item.getProduct().getName() + " is no longer available");
            }

            if (item.getProduct().getTrackQuantity() &&
                    item.getProduct().getStockQuantity() < item.getQuantity()) {
                errors.add("Insufficient stock for " + item.getProduct().getName());
            }
        }

        return errors;
    }

    @Override
    public void sendOrderConfirmation(Long orderId) {
        // Implementation for sending order confirmation email
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Send email logic here
    }

    @Override
    public void sendOrderStatusUpdate(Long orderId) {
        // Implementation for sending order status update
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Send notification logic here
    }

    @Override
    public void sendShippingNotification(Long orderId) {
        // Implementation for sending shipping notification
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Send shipping notification logic here
    }

    @Override
    public void sendDeliveryNotification(Long orderId) {
        // Implementation for sending delivery notification
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Send delivery notification logic here
    }
}