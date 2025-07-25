package com.ecommerce.service;

import com.ecommerce.entity.CustomOAuth2User;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.entity.enums.AuthProvider;
import com.ecommerce.entity.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    // Handle regular OAuth2 (non-OIDC)
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("=== CustomOAuth2UserService.loadUser() CALLED (OAuth2) ===");

        OAuth2User oauth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oauth2User);
    }

    // Handle OIDC (Google uses this)
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("=== CustomOAuth2UserService.loadUser() CALLED (OIDC) ===");

        OidcUserService oidcUserService = new OidcUserService();
        OidcUser oidcUser = oidcUserService.loadUser(userRequest);

        System.out.println("OIDC User loaded: " + oidcUser.getEmail());

        return (OidcUser) processOidcUser(userRequest, oidcUser);
    }

    private CustomOAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        System.out.println("=== processOAuth2User() STARTED ===");

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        System.out.println("Processing OAuth2 user - Email: " + email + ", Name: " + name + ", Provider: " + registrationId);

        User user = createOrUpdateUser(registrationId, email, name, oauth2User);
        return new CustomOAuth2User(oauth2User, user);
    }

    private CustomOAuth2User processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
        System.out.println("=== processOidcUser() STARTED ===");

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        System.out.println("Processing OIDC user - Email: " + email + ", Name: " + name + ", Provider: " + registrationId);

        User user = createOrUpdateUser(registrationId, email, name, oidcUser);
        return new CustomOAuth2User(oidcUser, user);
    }

    private User createOrUpdateUser(String registrationId, String email, String name, OAuth2User oauth2User) {
        if (email == null || email.isEmpty()) {
            System.err.println("❌ EMAIL IS NULL OR EMPTY!");
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        System.out.println("Checking if user exists in database...");
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            System.out.println("✅ User EXISTS in database: " + email);
            user = userOptional.get();
            if (!user.getProvider().equals(getAuthProvider(registrationId))) {
                System.err.println("❌ Provider mismatch! Expected: " + user.getProvider() + ", Got: " + getAuthProvider(registrationId));
                throw new OAuth2AuthenticationException(
                        "You're signed up with " + user.getProvider() + " account. Please use your " +
                                user.getProvider() + " account to login."
                );
            }
            user = updateExistingUser(user, name);
            System.out.println("✅ Updated existing user: " + user.getId());
        } else {
            System.out.println("❌ User DOES NOT EXIST - Creating new user: " + email);
            user = registerNewUser(registrationId, email, name, oauth2User);
            System.out.println("✅ Created new user with ID: " + user.getId());
        }

        return user;
    }

    private User registerNewUser(String registrationId, String email, String name, OAuth2User oauth2User) {
        System.out.println("=== REGISTERING NEW USER ===");

        User user = new User();

        String providerId = oauth2User.getAttribute("sub") != null ?
                oauth2User.getAttribute("sub").toString() : oauth2User.getAttribute("id").toString();

        System.out.println("Setting user properties:");
        System.out.println("- Email: " + email);
        System.out.println("- Name: " + name);
        System.out.println("- Provider: " + getAuthProvider(registrationId));
        System.out.println("- Provider ID: " + providerId);

        user.setProvider(getAuthProvider(registrationId));
        user.setProviderId(providerId);
        user.setEmail(email);
        user.setUsername(email); // Use email as username for OAuth users
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if (name != null) {
            String[] names = name.split(" ");
            user.setFirstName(names[0]);
            System.out.println("- First Name: " + names[0]);
            if (names.length > 1) {
                user.setLastName(names[names.length - 1]);
                System.out.println("- Last Name: " + names[names.length - 1]);
            }
        }

        // Assign default USER role
        user.setRoles(Collections.singleton(Role.ROLE_USER));
        System.out.println("- Role: " + Role.ROLE_USER);

        System.out.println("About to save user to database...");
        try {
            User savedUser = userRepository.save(user);
            System.out.println("✅ USER SAVED SUCCESSFULLY!");
            System.out.println("- User ID: " + savedUser.getId());
            System.out.println("- Email: " + savedUser.getEmail());
            System.out.println("- Provider: " + savedUser.getProvider());
            return savedUser;
        } catch (Exception e) {
            System.err.println("❌ ERROR SAVING USER: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getSimpleName());
            e.printStackTrace();
            throw new OAuth2AuthenticationException("Failed to save user: " + e.getMessage());
        }
    }

    private User updateExistingUser(User existingUser, String name) {
        System.out.println("=== UPDATING EXISTING USER ===");
        System.out.println("Existing user ID: " + existingUser.getId());
        System.out.println("Existing user email: " + existingUser.getEmail());

        if (name != null && !name.isEmpty()) {
            String[] names = name.split(" ");
            existingUser.setFirstName(names[0]);
            System.out.println("Updated first name: " + names[0]);
            if (names.length > 1) {
                existingUser.setLastName(names[names.length - 1]);
                System.out.println("Updated last name: " + names[names.length - 1]);
            }
        }
        existingUser.setUpdatedAt(LocalDateTime.now());

        try {
            User updatedUser = userRepository.save(existingUser);
            System.out.println("✅ USER UPDATED SUCCESSFULLY!");
            return updatedUser;
        } catch (Exception e) {
            System.err.println("❌ ERROR UPDATING USER: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException("Failed to update user: " + e.getMessage());
        }
    }

    private AuthProvider getAuthProvider(String registrationId) {
        System.out.println("Getting auth provider for: " + registrationId);
        switch (registrationId.toLowerCase()) {
            case "google":
                System.out.println("Provider: GOOGLE");
                return AuthProvider.GOOGLE;
            case "github":
                System.out.println("Provider: GITHUB");
                return AuthProvider.GITHUB;
            default:
                System.out.println("Provider: LOCAL (default)");
                return AuthProvider.LOCAL;
        }
    }
}