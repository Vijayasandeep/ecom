package com.ecommerce.service;

import com.ecommerce.controller.PaymentController.PaymentDetails;
import com.ecommerce.controller.PaymentController.PaymentRequest;
import com.ecommerce.controller.PaymentController.PaymentResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
@Service
public interface PaymentService {

    // Payment initiation
    PaymentResponse initiatePayment(Long userId, PaymentRequest request);

    // Stripe operations
    Map<String, String> createStripePaymentIntent(Long userId, Long orderId, BigDecimal amount);
    PaymentResponse confirmStripePayment(String paymentIntentId, Long orderId);
    void handleStripeWebhook(String payload, String signature);

    // Razorpay operations
    Map<String, Object> createRazorpayOrder(Long userId, Long orderId, BigDecimal amount);
    PaymentResponse verifyRazorpayPayment(Long orderId, String paymentId, String razorpayOrderId, String signature);
    void handleRazorpayWebhook(String payload, String signature);

    // PayPal operations
    Map<String, String> createPayPalOrder(Long userId, Long orderId, BigDecimal amount);
    PaymentResponse capturePayPalOrder(Long orderId, String payPalOrderId);

    // Payment management
    PaymentDetails getPaymentDetails(Long orderId);
    PaymentResponse refundPayment(Long paymentId, BigDecimal amount, String reason);
    Map<String, Object> getPaymentHistory(Long userId, int page, int size);

    // Payment validation
    boolean validatePayment(Long paymentId);
    boolean isPaymentSuccessful(Long paymentId);

    // Payment analytics
    Map<String, Object> getPaymentStats(Long userId);
    BigDecimal getTotalPayments(Long userId);

    // Webhook handling
    void processPaymentWebhook(String provider, String payload, String signature);
}