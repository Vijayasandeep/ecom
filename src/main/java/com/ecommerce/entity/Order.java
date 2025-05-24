package com.ecommerce.entity;

import com.ecommerce.entity.enums.OrderStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;

    private LocalDateTime orderDate;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private BigDecimal totalAmount;

    // Constructors, getters, setters
    public Order() {
        this.orderDate = LocalDateTime.now();
    }

    // Additional methods
//    public void addOrderItem(OrderItem item) {
//        orderItems.add(item);
//        item.setOrder(this);
//    }
}