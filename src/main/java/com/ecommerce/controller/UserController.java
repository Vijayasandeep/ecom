package com.ecommerce.controller;

import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal User user) {
        try {
            // Debug logging
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication: " + auth);
            System.out.println("Principal: " + (auth != null ? auth.getPrincipal() : "null"));
            System.out.println("Principal class: " + (auth != null && auth.getPrincipal() != null ? auth.getPrincipal().getClass() : "null"));
            System.out.println("User from @AuthenticationPrincipal: " + user);

            if (user == null) {
                // Fallback: try to get user manually
                if (auth != null && auth.getPrincipal() instanceof User) {
                    user = (User) auth.getPrincipal();
                } else if (auth != null && auth.getPrincipal() instanceof String) {
                    // If principal is username, fetch user
                    String email = (String) auth.getPrincipal();
                    user = userService.getUserByEmail(email);
                }

                if (user == null) {
                    return ResponseEntity.badRequest().body("User not authenticated properly");
                }
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateProfileRequest request) {
        try {
            if (user == null) {
                // Fallback authentication handling
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof User) {
                    user = (User) auth.getPrincipal();
                } else {
                    return ResponseEntity.badRequest().body("User not authenticated");
                }
            }

            User updatedUser = userService.updateProfile(user.getId(), request);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ChangePasswordRequest request) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            userService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(
            @AuthenticationPrincipal User user,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            String avatarUrl = userService.uploadAvatar(user.getId(), file);
            return ResponseEntity.ok(avatarUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Address Management
    @GetMapping("/addresses")
    public ResponseEntity<?> getUserAddresses(@AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            List<Address> addresses = userService.getUserAddresses(user.getId());
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> addAddress(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid AddAddressRequest request) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            Address address = userService.addAddress(user.getId(), request);
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<?> updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long addressId,
            @RequestBody @Valid UpdateAddressRequest request) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            Address address = userService.updateAddress(user.getId(), addressId, request);
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<?> deleteAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long addressId) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            userService.deleteAddress(user.getId(), addressId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/addresses/{addressId}/set-default")
    public ResponseEntity<?> setDefaultAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long addressId) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            userService.setDefaultAddress(user.getId(), addressId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getUserDashboard(@AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            UserDashboard dashboard = userService.getUserDashboard(user.getId());
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateAccount(@AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            userService.deactivateAccount(user.getId());
            return ResponseEntity.ok("Account deactivated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // DTOs for user operations
    public static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String phone;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class AddAddressRequest {
        private String street;
        private String city;
        private String state;
        private String country;
        private String zipCode;
        private boolean isDefault;

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
        public boolean isDefault() { return isDefault; }
        public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    }

    public static class UpdateAddressRequest extends AddAddressRequest {
        // Inherits all fields from AddAddressRequest
    }

    public static class UserDashboard {
        private int totalOrders;
        private int pendingOrders;
        private int completedOrders;
        private int cartItems;
        private int wishlistItems;

        public UserDashboard(int totalOrders, int pendingOrders, int completedOrders, int cartItems, int wishlistItems) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.completedOrders = completedOrders;
            this.cartItems = cartItems;
            this.wishlistItems = wishlistItems;
        }

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
        public int getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }
        public int getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }
        public int getCartItems() { return cartItems; }
        public void setCartItems(int cartItems) { this.cartItems = cartItems; }
        public int getWishlistItems() { return wishlistItems; }
        public void setWishlistItems(int wishlistItems) { this.wishlistItems = wishlistItems; }
    }
}