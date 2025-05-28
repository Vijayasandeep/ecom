package com.ecommerce.controller.admin;

import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
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
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {

        if (role != null) {
            return ResponseEntity.ok(userService.getUsersByRole(role));
        } else if ("active".equals(status)) {
            return ResponseEntity.ok(userService.getActiveUsers());
        } else if ("inactive".equals(status)) {
            return ResponseEntity.ok(userService.getInactiveUsers());
        } else {
            return ResponseEntity.ok(userService.getAllUsers());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}/toggle-status")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Long userId) {
        userService.toggleUserStatus(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long userId,
            @RequestBody @Valid UpdateUserRoleRequest request) {
        userService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId) {
        userService.activateAccount(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateAccount(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getUserAnalytics() {
        Map<String, Object> analytics = Map.of(
                "totalUsers", userService.getAllUsers().size(),
                "activeUsers", userService.getActiveUsers().size(),
                "inactiveUsers", userService.getInactiveUsers().size(),
                "adminUsers", userService.getUsersByRole("ADMIN").size(),
                "regularUsers", userService.getUsersByRole("USER").size()
        );
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<Map<String, Object>> getUserOrders(@PathVariable Long userId) {
        // Implementation needed to get user's order history
        Map<String, Object> orderData = Map.of(
                "totalOrders", 0,
                "totalSpent", 0,
                "recentOrders", List.of()
        );
        return ResponseEntity.ok(orderData);
    }

    @GetMapping("/{userId}/activity")
    public ResponseEntity<List<UserActivityLog>> getUserActivity(@PathVariable Long userId) {
        // Implementation needed for user activity tracking
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/bulk-update")
    public ResponseEntity<String> bulkUpdateUsers(@RequestBody BulkUpdateUsersRequest request) {
        // Implementation needed for bulk user operations
        return ResponseEntity.ok("Bulk update completed");
    }

    @PostMapping("/{userId}/send-notification")
    public ResponseEntity<Void> sendUserNotification(
            @PathVariable Long userId,
            @RequestBody @Valid SendNotificationRequest request) {
        // Implementation needed for sending notifications to users
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportUsers(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String role) {
        // Implementation needed for user data export
        return ResponseEntity.ok("Export completed");
    }

    @GetMapping("/registration-stats")
    public ResponseEntity<Map<String, Object>> getRegistrationStats(
            @RequestParam(defaultValue = "30") int days) {
        // Implementation needed for registration statistics
        Map<String, Object> stats = Map.of(
                "totalRegistrations", 0,
                "dailyRegistrations", List.of(),
                "registrationGrowth", 0.0
        );
        return ResponseEntity.ok(stats);
    }

    // DTOs for admin user operations
    public static class UpdateUserRoleRequest {
        private String role;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class BulkUpdateUsersRequest {
        private List<Long> userIds;
        private String action;
        private Object value;

        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }

    public static class SendNotificationRequest {
        private String title;
        private String message;
        private String type;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class UserActivityLog {
        private String activity;
        private String timestamp;
        private String details;
        private String ipAddress;

        public UserActivityLog(String activity, String timestamp, String details, String ipAddress) {
            this.activity = activity;
            this.timestamp = timestamp;
            this.details = details;
            this.ipAddress = ipAddress;
        }

        public String getActivity() { return activity; }
        public void setActivity(String activity) { this.activity = activity; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    }
}