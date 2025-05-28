package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Basic user order queries
//    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    // Order status queries
    Page<Order> findByUserIdAndStatusOrderByOrderDateDesc(Long userId, OrderStatus status, Pageable pageable);
    List<Order> findByStatus(OrderStatus status);
    long countByStatus(OrderStatus status);
    long countByUserIdAndStatus(Long userId, OrderStatus status);

    // Date range queries
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByUserIdAndOrderDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // Order analytics
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.user.id = :userId AND o.status = 'DELIVERED'")
    BigDecimal getTotalSpentByUser(@Param("userId") Long userId);

    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.user.id = :userId AND o.status = 'DELIVERED'")
    BigDecimal getAverageOrderValueByUser(@Param("userId") Long userId);

    // Recent orders
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC LIMIT :limit")
    List<Order> findRecentOrdersByUser(@Param("userId") Long userId, @Param("limit") int limit);

    // Admin queries
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    Page<Order> findAllOrdersForAdmin(Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.orderDate DESC")
    Page<Order> findOrdersByStatusForAdmin(@Param("status") OrderStatus status, Pageable pageable);

    // Revenue analytics
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    long getOrderCountInPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Top customers
    @Query("SELECT o.user.id, COUNT(o), SUM(o.totalAmount) FROM Order o " +
            "WHERE o.status = 'DELIVERED' " +
            "GROUP BY o.user.id " +
            "ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> findTopCustomers(Pageable pageable);

    // Orders by amount range
    List<Order> findByTotalAmountBetweenOrderByOrderDateDesc(BigDecimal minAmount, BigDecimal maxAmount);

    // Pending orders (for processing)
    @Query("SELECT o FROM Order o WHERE o.status IN ('PENDING', 'CONFIRMED') ORDER BY o.orderDate ASC")
    List<Order> findPendingOrders();

    // Orders requiring attention
    @Query("SELECT o FROM Order o WHERE o.status = 'PROCESSING' AND o.orderDate < :cutoffDate")
    List<Order> findStuckOrders(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Cancelled orders
    List<Order> findByStatusAndOrderDateBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // Monthly order statistics
    @Query("SELECT YEAR(o.orderDate), MONTH(o.orderDate), COUNT(o), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE o.status = 'DELIVERED' " +
            "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) " +
            "ORDER BY YEAR(o.orderDate) DESC, MONTH(o.orderDate) DESC")
    List<Object[]> getMonthlyOrderStats();

    // Orders with specific products
    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.orderItems oi " +
            "WHERE oi.product.id = :productId")
    List<Order> findOrdersContainingProduct(@Param("productId") Long productId);
}