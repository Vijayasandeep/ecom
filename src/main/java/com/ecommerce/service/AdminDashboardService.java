package com.ecommerce.service;


import com.ecommerce.controller.admin.AdminDashboardController;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
@Service
public interface AdminDashboardService {

    // Dashboard overview
    Map<String, Object> getDashboardOverview();
    AdminDashboardController.DashboardStats getDashboardStats();

    // Sales and revenue analytics
    Map<String, Object> getSalesAnalytics(int days);
    Map<String, Object> getRevenueChart(LocalDate startDate, LocalDate endDate, String period);
    Map<String, Object> getMonthlyRevenue();
    Map<String, Object> getYearlyRevenue();

    // Product analytics
    Map<String, Object> getTopProducts(int limit, String sortBy);
    Map<String, Object> getTopCategories(int limit);
    Map<String, Object> getProductPerformance();

    // Customer analytics
    Map<String, Object> getCustomerAnalytics();
    Map<String, Object> getCustomerSegmentation();
    Map<String, Object> getCustomerLifetimeValue();

    // Inventory management
    Map<String, Object> getInventoryAlerts();
    Map<String, Object> getStockAnalytics();

    // Order management
    Map<String, Object> getRecentOrders(int limit);
    Map<String, Object> getOrderAnalytics();
    Map<String, Object> getOrderStatusDistribution();

    // Performance metrics
    Map<String, Object> getPerformanceMetrics();
    Map<String, Object> getConversionMetrics();
    Map<String, Object> getGrowthMetrics(int months);

    // Financial analytics
    Map<String, Object> getFinancialSummary();
    Map<String, Object> getProfitAnalysis();

    // System health
    Map<String, Object> getSystemHealth();
    Map<String, Object> getActivityLogs();
}