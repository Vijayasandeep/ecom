package com.ecommerce.service.impl;

import com.ecommerce.controller.UserController.AddAddressRequest;
import com.ecommerce.controller.UserController.UpdateAddressRequest;
import com.ecommerce.controller.UserController.UpdateProfileRequest;
import com.ecommerce.controller.UserController.UserDashboard;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        return userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        // Generate unique filename and save file (implementation needed)
        String avatarUrl = "/uploads/avatars/" + userId + "_" + System.currentTimeMillis() + ".jpg";

        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return avatarUrl;
    }

    @Override
    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public void activateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> getUserAddresses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return user.getAddresses();
    }

    @Override
    public Address addAddress(Long userId, AddAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Address address = new Address();
        address.setUser(user);
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());
        address.setDefault(request.isDefault());

        // If this is set as default, make sure no other address is default
        if (request.isDefault()) {
            user.getAddresses().forEach(addr -> addr.setDefault(false));
        }

        user.getAddresses().add(address);
        userRepository.save(user);

        return address;
    }

    @Override
    public Address updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Address address = user.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());

        if (request.isDefault()) {
            user.getAddresses().forEach(addr -> addr.setDefault(false));
            address.setDefault(true);
        }

        userRepository.save(user);
        return address;
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.getAddresses().removeIf(address -> address.getId().equals(addressId));
        userRepository.save(user);
    }

    @Override
    public void setDefaultAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.getAddresses().forEach(addr -> {
            addr.setDefault(addr.getId().equals(addressId));
        });

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Address getDefaultAddress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return user.getAddresses().stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDashboard getUserDashboard(Long userId) {
        // Implementation would require Order and Cart repositories
        return new UserDashboard(0, 0, 0, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email, Long userId) {
        if (userId == null) {
            return !userRepository.existsByEmail(email);
        }
        Optional<User> existingUser = userRepository.findByEmail(email);
        return existingUser.isEmpty() || existingUser.get().getId().equals(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPhoneUnique(String phone, Long userId) {
        // Implementation would require phone field queries
        return true; // Placeholder
    }

    @Override
    public void verifyEmail(Long userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verify token logic here
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public void verifyPhone(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verify code logic here
        user.setPhoneVerified(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(String query) {
        // Implementation would require custom query
        return userRepository.findAll(); // Placeholder
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String role) {
        // Implementation would require role-based query
        return userRepository.findAll(); // Placeholder
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        // Implementation would require active status query
        return userRepository.findAll(); // Placeholder
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getInactiveUsers() {
        // Implementation would require inactive status query
        return userRepository.findAll(); // Placeholder
    }

    @Override
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Generate reset token and send email
        // Implementation needed
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // Validate token and reset password
        // Implementation needed
    }

    @Override
    public void updateNotificationPreferences(Long userId, boolean emailNotifications, boolean smsNotifications) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update notification preferences
        // Would need additional fields in User entity
        userRepository.save(user);
    }

    @Override
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    @Override
    public void updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Convert string to Role enum and update
        // Implementation depends on Role enum structure
        userRepository.save(user);
    }
}