package com.ecommerce.service;

import com.ecommerce.controller.UserController.AddAddressRequest;
import com.ecommerce.controller.UserController.UpdateAddressRequest;
import com.ecommerce.controller.UserController.UpdateProfileRequest;
import com.ecommerce.controller.UserController.UserDashboard;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
@Service
public interface UserService {

    // User profile management
    User updateProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, String currentPassword, String newPassword);
    String uploadAvatar(Long userId, MultipartFile file);
    void deactivateAccount(Long userId);
    void activateAccount(Long userId);

    // User retrieval
    Optional<User> getUserById(Long userId);
    User getUserByEmail(String email);
    List<User> getAllUsers();

    // Address management
    List<Address> getUserAddresses(Long userId);
    Address addAddress(Long userId, AddAddressRequest request);
    Address updateAddress(Long userId, Long addressId, UpdateAddressRequest request);
    void deleteAddress(Long userId, Long addressId);
    void setDefaultAddress(Long userId, Long addressId);
    Address getDefaultAddress(Long userId);

    // User dashboard and analytics
    UserDashboard getUserDashboard(Long userId);

    // User validation and verification
    boolean isEmailUnique(String email, Long userId);
    boolean isPhoneUnique(String phone, Long userId);
    void verifyEmail(Long userId, String token);
    void verifyPhone(Long userId, String code);

    // User search and filtering
    List<User> searchUsers(String query);
    List<User> getUsersByRole(String role);
    List<User> getActiveUsers();
    List<User> getInactiveUsers();

    // Password reset
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);

    // User notifications
    void updateNotificationPreferences(Long userId, boolean emailNotifications, boolean smsNotifications);

    // Admin operations
    void toggleUserStatus(Long userId);
    void updateUserRole(Long userId, String role);
}