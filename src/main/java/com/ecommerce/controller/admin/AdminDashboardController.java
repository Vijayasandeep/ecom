package com.ecommerce.controller.admin;

import com.ecommerce.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> overview = adminDashboardService.getDashboardOverview();
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        DashboardStats stats = adminDashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/sales-analytics")
    public ResponseEntity<Map<String, Object>> getSalesAnalytics(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> analytics = adminDashboardService.getSalesAnalytics(days);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<Map<String, Object>> getRevenueChart(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "daily") String period) {
        Map<String, Object> chartData = adminDashboardService.getRevenueChart(startDate, endDate, period);
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/top-products")
    public ResponseEntity<Map<String, Object>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "sales") String sortBy) {
        Map<String, Object> topProducts = adminDashboardService.getTopProducts(limit, sortBy);
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/top-categories")
    public ResponseEntity<Map<String, Object>> getTopCategories(
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> topCategories = adminDashboardService.getTopCategories(limit);
        return ResponseEntity.ok(topCategories);
    }

    @GetMapping("/customer-analytics")
    public ResponseEntity<Map<String, Object>> getCustomerAnalytics() {
        Map<String, Object> analytics = adminDashboardService.getCustomerAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/inventory-alerts")
    public ResponseEntity<Map<String, Object>> getInventoryAlerts() {
        Map<String, Object> alerts = adminDashboardService.getInventoryAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<Map<String, Object>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> recentOrders = adminDashboardService.getRecentOrders(limit);
        return ResponseEntity.ok(recentOrders);
    }

    @GetMapping("/performance-metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> metrics = adminDashboardService.getPerformanceMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/growth-metrics")
    public ResponseEntity<Map<String, Object>> getGrowthMetrics(
            @RequestParam(defaultValue = "12") int months) {
        Map<String, Object> growth = adminDashboardService.getGrowthMetrics(months);
        return ResponseEntity.ok(growth);
    }

    // DTO for dashboard statistics
    public static class DashboardStats {
        private long totalUsers;
        private long totalProducts;
        private long totalOrders;
        private long totalRevenue;
        private long pendingOrders;
        private long lowStockProducts;
        private long newCustomersToday;
        private long ordersToday;
        private double conversionRate;
        private double averageOrderValue;

        public DashboardStats(long totalUsers, long totalProducts, long totalOrders, long totalRevenue,
                              long pendingOrders, long lowStockProducts, long newCustomersToday, long ordersToday,
                              double conversionRate, double averageOrderValue) {
            this.totalUsers = totalUsers;
            this.totalProducts = totalProducts;
            this.totalOrders = totalOrders;
            this.totalRevenue = totalRevenue;
            this.pendingOrders = pendingOrders;
            this.lowStockProducts = lowStockProducts;
            this.newCustomersToday = newCustomersToday;
            this.ordersToday = ordersToday;
            this.conversionRate = conversionRate;
            this.averageOrderValue = averageOrderValue;
        }

        // Getters and setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        public long getTotalProducts() { return totalProducts; }
        public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
        public long getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(long totalRevenue) { this.totalRevenue = totalRevenue; }
        public long getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
        public long getLowStockProducts() { return lowStockProducts; }
        public void setLowStockProducts(long lowStockProducts) { this.lowStockProducts = lowStockProducts; }
        public long getNewCustomersToday() { return newCustomersToday; }
        public void setNewCustomersToday(long newCustomersToday) { this.newCustomersToday = newCustomersToday; }
        public long getOrdersToday() { return ordersToday; }
        public void setOrdersToday(long ordersToday) { this.ordersToday = ordersToday; }
        public double getConversionRate() { return conversionRate; }
        public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
        public double getAverageOrderValue() { return averageOrderValue; }
        public void setAverageOrderValue(double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    }
}