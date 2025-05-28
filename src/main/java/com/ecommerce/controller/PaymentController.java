package com.ecommerce.controller;

import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.PaymentMethod;
import com.ecommerce.entity.enums.PaymentStatus;
import com.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid PaymentRequest request) {
        PaymentResponse response = paymentService.initiatePayment(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stripe/create-intent")
    public ResponseEntity<Map<String, String>> createStripePaymentIntent(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid StripePaymentRequest request) {
        Map<String, String> response = paymentService.createStripePaymentIntent(
                user.getId(), request.getOrderId(), request.getAmount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stripe/confirm")
    public ResponseEntity<PaymentResponse> confirmStripePayment(
            @RequestBody @Valid StripeConfirmRequest request) {
        PaymentResponse response = paymentService.confirmStripePayment(
                request.getPaymentIntentId(), request.getOrderId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/razorpay/create-order")
    public ResponseEntity<Map<String, Object>> createRazorpayOrder(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid RazorpayOrderRequest request) {
        Map<String, Object> response = paymentService.createRazorpayOrder(
                user.getId(), request.getOrderId(), request.getAmount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/razorpay/verify")
    public ResponseEntity<PaymentResponse> verifyRazorpayPayment(
            @RequestBody @Valid RazorpayVerifyRequest request) {
        PaymentResponse response = paymentService.verifyRazorpayPayment(
                request.getOrderId(), request.getPaymentId(),
                request.getRazorpayOrderId(), request.getSignature());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/paypal/create-order")
    public ResponseEntity<Map<String, String>> createPayPalOrder(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid PayPalOrderRequest request) {
        Map<String, String> response = paymentService.createPayPalOrder(
                user.getId(), request.getOrderId(), request.getAmount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/paypal/capture")
    public ResponseEntity<PaymentResponse> capturePayPalOrder(
            @RequestBody @Valid PayPalCaptureRequest request) {
        PaymentResponse response = paymentService.capturePayPalOrder(
                request.getOrderId(), request.getPayPalOrderId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDetails> getPaymentDetails(@PathVariable Long orderId) {
        PaymentDetails details = paymentService.getPaymentDetails(orderId);
        return ResponseEntity.ok(details);
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long paymentId,
            @RequestBody @Valid RefundRequest request) {
        PaymentResponse response = paymentService.refundPayment(
                paymentId, request.getAmount(), request.getReason());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/history")
    public ResponseEntity<Map<String, Object>> getPaymentHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> history = paymentService.getPaymentHistory(user.getId(), page, size);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        paymentService.handleStripeWebhook(payload, signature);
        return ResponseEntity.ok("Webhook handled successfully");
    }

    @PostMapping("/webhook/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleRazorpayWebhook(payload, signature);
        return ResponseEntity.ok("Webhook handled successfully");
    }

    // DTOs for payment operations
    public static class PaymentRequest {
        private Long orderId;
        private PaymentMethod paymentMethod;
        private BigDecimal amount;
        private String currency = "USD";
        private String returnUrl;
        private String cancelUrl;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getReturnUrl() { return returnUrl; }
        public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
        public String getCancelUrl() { return cancelUrl; }
        public void setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; }
    }

    public static class StripePaymentRequest {
        private Long orderId;
        private BigDecimal amount;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class StripeConfirmRequest {
        private String paymentIntentId;
        private Long orderId;

        public String getPaymentIntentId() { return paymentIntentId; }
        public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
    }

    public static class RazorpayOrderRequest {
        private Long orderId;
        private BigDecimal amount;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class RazorpayVerifyRequest {
        private Long orderId;
        private String paymentId;
        private String razorpayOrderId;
        private String signature;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        public String getRazorpayOrderId() { return razorpayOrderId; }
        public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }
    }

    public static class PayPalOrderRequest {
        private Long orderId;
        private BigDecimal amount;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class PayPalCaptureRequest {
        private Long orderId;
        private String payPalOrderId;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getPayPalOrderId() { return payPalOrderId; }
        public void setPayPalOrderId(String payPalOrderId) { this.payPalOrderId = payPalOrderId; }
    }

    public static class RefundRequest {
        private BigDecimal amount;
        private String reason;

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class PaymentResponse {
        private Long paymentId;
        private PaymentStatus status;
        private String message;
        private String transactionId;
        private Map<String, Object> additionalData;

        public PaymentResponse(Long paymentId, PaymentStatus status, String message, String transactionId) {
            this.paymentId = paymentId;
            this.status = status;
            this.message = message;
            this.transactionId = transactionId;
        }

        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
        public PaymentStatus getStatus() { return status; }
        public void setStatus(PaymentStatus status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public Map<String, Object> getAdditionalData() { return additionalData; }
        public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }
    }

    public static class PaymentDetails {
        private Long paymentId;
        private Long orderId;
        private PaymentMethod paymentMethod;
        private PaymentStatus status;
        private BigDecimal amount;
        private String currency;
        private String transactionId;
        private String createdAt;

        public PaymentDetails(Long paymentId, Long orderId, PaymentMethod paymentMethod, PaymentStatus status,
                              BigDecimal amount, String currency, String transactionId, String createdAt) {
            this.paymentId = paymentId;
            this.orderId = orderId;
            this.paymentMethod = paymentMethod;
            this.status = status;
            this.amount = amount;
            this.currency = currency;
            this.transactionId = transactionId;
            this.createdAt = createdAt;
        }

        // Getters and setters
        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
        public PaymentStatus getStatus() { return status; }
        public void setStatus(PaymentStatus status) { this.status = status; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}